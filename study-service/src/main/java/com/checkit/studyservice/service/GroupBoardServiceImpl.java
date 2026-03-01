package com.checkit.studyservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.dto.*;
import com.checkit.studyservice.entity.GroupBoardComment;
import com.checkit.studyservice.entity.GroupBoardEmpathy;
import com.checkit.studyservice.entity.GroupBoardPost;
import com.checkit.studyservice.repository.GroupBoardCommentRepository;
import com.checkit.studyservice.repository.GroupBoardEmpathyRepository;
import com.checkit.studyservice.repository.GroupBoardPostRepository;
import com.checkit.studyservice.repository.StudyGroupRepository;
import com.checkit.studyservice.repository.StudyUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupBoardServiceImpl implements GroupBoardService {

    private final StudyUserRepository studyUserRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final GroupBoardPostRepository postRepository;
    private final GroupBoardCommentRepository commentRepository;
    private final GroupBoardEmpathyRepository empathyRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BoardPostListItemRes> getPostList(UUID actor, Long groupId, Pageable pageable) {
        ensureGroupMember(actor, groupId);
        Page<GroupBoardPost> page = postRepository.findByGroupIdAndDeletedAtIsNullOrderByCreatedAtDesc(groupId, pageable);
        List<GroupBoardPost> content = page.getContent();
        if (content.isEmpty()) {
            return page.map(p -> BoardPostListItemRes.builder()
                    .postId(p.getPostId())
                    .title(p.getTitle())
                    .authorUserId(p.getAuthorUserId())
                    .empathyCount(0L)
                    .commentCount(0L)
                    .build());
        }
        List<Long> postIds = content.stream().map(GroupBoardPost::getPostId).toList();
        Map<Long, Long> empathyCounts = empathyRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(GroupBoardEmpathy::getPostId, Collectors.counting()));
        Map<Long, Long> commentCounts = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return page.map(p -> BoardPostListItemRes.builder()
                .postId(p.getPostId())
                .title(p.getTitle())
                .authorUserId(p.getAuthorUserId())
                .empathyCount(empathyCounts.getOrDefault(p.getPostId(), 0L))
                .commentCount(commentCounts.getOrDefault(p.getPostId(), 0L))
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public BoardPostDetailRes getPostDetail(UUID actor, Long groupId, Long postId) {
        ensureGroupMember(actor, groupId);
        GroupBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if (post.isDeleted() || !post.getGroupId().equals(groupId)) {
            throw new BusinessException(CommonCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        long empathyCount = empathyRepository.countByPostId(postId);
        boolean empathized = actor != null && empathyRepository.findByPostIdAndUserId(postId, actor).isPresent();
        long commentCount = commentRepository.countByPostIdAndDeletedAtIsNull(postId);
        List<GroupBoardComment> comments = commentRepository.findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
        List<BoardPostDetailRes.BoardCommentItemRes> commentItems = comments.stream()
                .map(c -> BoardPostDetailRes.BoardCommentItemRes.builder()
                        .commentId(c.getCommentId())
                        .userId(c.getUserId())
                        .createdAt(c.getCreatedAt())
                        .content(c.getContent())
                        .isMine(actor != null && actor.equals(c.getUserId()))
                        .build())
                .toList();

        return BoardPostDetailRes.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .authorUserId(post.getAuthorUserId())
                .createdAt(post.getCreatedAt())
                .content(post.getContent())
                .empathyCount(empathyCount)
                .commentCount(commentCount)
                .empathized(empathized)
                .comments(commentItems)
                .build();
    }

    @Override
    public Long createPost(UUID actor, Long groupId, BoardPostCreateReq request) {
        ensureGroupMember(actor, groupId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        GroupBoardPost post = GroupBoardPost.builder()
                .groupId(groupId)
                .authorUserId(actor)
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return postRepository.save(post).getPostId();
    }

    @Override
    public void updatePost(UUID actor, Long groupId, Long postId, BoardPostUpdateReq request) {
        ensureGroupMember(actor, groupId);
        GroupBoardPost post = getPostForGroup(postId, groupId);
        if (!post.getAuthorUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "본인이 작성한 게시글만 수정할 수 있습니다.");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        postRepository.save(post);
    }

    @Override
    public void deletePost(UUID actor, Long groupId, Long postId) {
        ensureGroupMember(actor, groupId);
        GroupBoardPost post = getPostForGroup(postId, groupId);
        if (!post.getAuthorUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "본인이 작성한 게시글만 삭제할 수 있습니다.");
        }
        post.softDelete(actor);
        postRepository.save(post);
    }

    @Override
    public Long createComment(UUID actor, Long groupId, Long postId, BoardCommentCreateReq request) {
        ensureGroupMember(actor, groupId);
        GroupBoardPost post = getPostForGroup(postId, groupId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        GroupBoardComment comment = GroupBoardComment.builder()
                .postId(post.getPostId())
                .userId(actor)
                .content(request.getContent())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return commentRepository.save(comment).getCommentId();
    }

    @Override
    public void updateComment(UUID actor, Long groupId, Long postId, Long commentId, BoardCommentUpdateReq request) {
        ensureGroupMember(actor, groupId);
        getPostForGroup(postId, groupId);
        GroupBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!comment.getPostId().equals(postId) || comment.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        }
        if (!comment.getUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        comment.setContent(request.getContent());
        comment.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(UUID actor, Long groupId, Long postId, Long commentId) {
        ensureGroupMember(actor, groupId);
        getPostForGroup(postId, groupId);
        GroupBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!comment.getPostId().equals(postId) || comment.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        }
        if (!comment.getUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        comment.softDelete(actor);
        commentRepository.save(comment);
    }

    @Override
    public BoardEmpathyRes toggleEmpathy(UUID actor, Long groupId, Long postId) {
        ensureGroupMember(actor, groupId);
        getPostForGroup(postId, groupId);
        var existing = empathyRepository.findByPostIdAndUserId(postId, actor);
        if (existing.isPresent()) {
            empathyRepository.delete(existing.get());
            long count = empathyRepository.countByPostId(postId);
            return BoardEmpathyRes.builder().empathized(false).empathyCount(count).build();
        } else {
            GroupBoardEmpathy e = GroupBoardEmpathy.builder()
                    .postId(postId)
                    .userId(actor)
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();
            empathyRepository.save(e);
            long count = empathyRepository.countByPostId(postId);
            return BoardEmpathyRes.builder().empathized(true).empathyCount(count).build();
        }
    }

    private void ensureGroupMember(UUID actor, Long groupId) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        var group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "그룹원만 게시판을 이용할 수 있습니다.");
        }
    }

    private GroupBoardPost getPostForGroup(Long postId, Long groupId) {
        GroupBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if (post.isDeleted() || !post.getGroupId().equals(groupId)) {
            throw new BusinessException(CommonCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        return post;
    }
}
