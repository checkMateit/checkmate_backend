package com.checkit.studyservice.service;

import com.checkit.studyservice.dto.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 스터디 그룹 게시판 서비스.
 * 그룹원(그룹장 포함)만 게시글·댓글·공감 가능.
 */
public interface GroupBoardService {

    /** 게시글 목록 (제목, 공감수, 댓글수) */
    Page<BoardPostListItemRes> getPostList(UUID actor, Long groupId, Pageable pageable);

    /** 게시글 상세 (제목, 작성자, 작성일시, 내용, 공감수, 댓글수, 댓글 목록) */
    BoardPostDetailRes getPostDetail(UUID actor, Long groupId, Long postId);

    /** 게시글 작성 */
    Long createPost(UUID actor, Long groupId, BoardPostCreateReq request);

    /** 게시글 수정 (작성자만) */
    void updatePost(UUID actor, Long groupId, Long postId, BoardPostUpdateReq request);

    /** 게시글 삭제 (작성자만, soft delete) */
    void deletePost(UUID actor, Long groupId, Long postId);

    /** 댓글 작성 */
    Long createComment(UUID actor, Long groupId, Long postId, BoardCommentCreateReq request);

    /** 댓글 수정 (작성자만) */
    void updateComment(UUID actor, Long groupId, Long postId, Long commentId, BoardCommentUpdateReq request);

    /** 댓글 삭제 (작성자만, soft delete) */
    void deleteComment(UUID actor, Long groupId, Long postId, Long commentId);

    /** 공감 토글 (누르면 추가, 다시 누르면 취소) */
    BoardEmpathyRes toggleEmpathy(UUID actor, Long groupId, Long postId);
}
