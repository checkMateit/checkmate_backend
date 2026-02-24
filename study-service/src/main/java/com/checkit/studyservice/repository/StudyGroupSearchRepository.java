package com.checkit.studyservice.repository;

import com.checkit.studyservice.dto.StudyGroupSearchCond;
import com.checkit.studyservice.entity.GroupStatus;
import com.checkit.studyservice.entity.StudyGroup;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.querydsl.core.types.Order.ASC;

import static com.checkit.studyservice.entity.QGroupVerificationMethod.groupVerificationMethod;
import static com.checkit.studyservice.entity.QHashtag.hashtag;
import static com.checkit.studyservice.entity.QStudyGroup.studyGroup;
import static com.checkit.studyservice.entity.QStudyGroupTag.studyGroupTag;

/**
 * QueryDSL 기반 스터디 그룹 검색.
 * <p>
 * QueryDSL이란: JPA 엔티티를 기반으로 Q클래스(QStudyGroup 등)가 생성되고,
 * 이를 이용해 Java 코드로 타입 세이프한 JPQL을 작성할 수 있게 해주는 라이브러리입니다.
 * 조건이 많을 때 동적 where 절을 if문으로 쉽게 붙일 수 있어 검색/필터 구현에 적합합니다.
 */
@Repository
@RequiredArgsConstructor
public class StudyGroupSearchRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 검색 조건에 맞는 스터디 그룹 ID 목록을 페이징하여 조회합니다.
     * <p>
     * 기본 동작: 삭제되지 않음(deletedAt is null), 모집 중(status = RECRUITING)만 대상.
     * 조건이 있으면 and로 추가됩니다.
     */
    public Page<StudyGroup> search(StudyGroupSearchCond cond, Pageable pageable) {
        // 1) FROM study_groups g 에 해당하는 Q클래스. QueryDSL은 엔티티마다 Q클래스를 생성합니다.
        JPAQuery<StudyGroup> query = queryFactory
                .selectFrom(studyGroup)
                .where(
                        deletedAndRecruitingOnly(),  // 기본: 삭제·마감 제외
                        categoryEq(cond.getCategory()),
                        keywordLike(cond.getKeyword()),
                        verificationMethodIn(cond.getVerificationMethods()),
                        minMembersGoe(cond.getMinMembers()),
                        maxMembersLoe(cond.getMaxMembers()),
                        startDateBetween(cond.getStartDateFrom(), cond.getStartDateTo()),
                        isIndefiniteEq(cond.getIsIndefinite()),
                        joinTypeEq(cond.getJoinType())
                )
                .distinct();  // 서브쿼리/조인으로 인한 중복 제거

        // 2) 정렬: "createdAt,desc" 형태를 OrderSpecifier로 변환
        OrderSpecifier<?>[] orderSpecifiers = orderBy(cond.getSort());
        if (orderSpecifiers != null && orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        }

        // 3) 페이징 적용 후 fetch
        List<StudyGroup> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 4) total count 쿼리: 동일 조건으로 개수만 조회 (페이징 없음)
        JPAQuery<Long> countQuery = queryFactory
                .select(studyGroup.groupId.countDistinct())
                .from(studyGroup)
                .where(
                        deletedAndRecruitingOnly(),
                        categoryEq(cond.getCategory()),
                        keywordLike(cond.getKeyword()),
                        verificationMethodIn(cond.getVerificationMethods()),
                        minMembersGoe(cond.getMinMembers()),
                        maxMembersLoe(cond.getMaxMembers()),
                        startDateBetween(cond.getStartDateFrom(), cond.getStartDateTo()),
                        isIndefiniteEq(cond.getIsIndefinite()),
                        joinTypeEq(cond.getJoinType())
                );
        Long totalCount = countQuery.fetchOne();
        long total = totalCount != null ? totalCount : 0;

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 추천 스터디: 선호 카테고리에 해당하는 모집 중 그룹을 랜덤 순으로 limit건 조회.
     */
    public List<StudyGroup> findRecommended(List<com.checkit.studyservice.entity.Category> categories, int limit) {
        if (limit <= 0) return List.of();
        BooleanExpression categoryIn = categoryIn(categories);
        return queryFactory
                .selectFrom(studyGroup)
                .where(
                        deletedAndRecruitingOnly(),
                        categoryIn
                )
                .orderBy(new OrderSpecifier<>(ASC, Expressions.numberTemplate(Double.class, "function('RANDOM')")))
                .limit(limit)
                .fetch();
    }

    private BooleanExpression categoryIn(List<com.checkit.studyservice.entity.Category> categories) {
        if (categories == null || categories.isEmpty()) return null;
        return studyGroup.category.in(categories);
    }

    // ---- 기본 조건: 삭제·마감 제외 ----
    private BooleanExpression deletedAndRecruitingOnly() {
        return studyGroup.deletedAt.isNull()
                .and(studyGroup.status.eq(GroupStatus.RECRUITING));
    }

    // ---- 기본 필터 ----
    private BooleanExpression categoryEq(com.checkit.studyservice.entity.Category category) {
        return category != null ? studyGroup.category.eq(category) : null;
    }

    /**
     * 키워드: 그룹명(title) 또는 해시태그(name/normalizedName)에 포함되면 true.
     * 서브쿼리로 "이 groupId에 해당 키워드를 가진 태그가 있는지" 또는 title like 로 처리.
     */
    private BooleanExpression keywordLike(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        // 그룹명에 포함 OR 해시태그 매핑 중 하나라도 키워드 일치
        BooleanExpression titleMatch = studyGroup.title.lower().like(pattern);
        BooleanExpression hashtagMatch = com.querydsl.jpa.JPAExpressions
                .selectOne()
                .from(studyGroupTag)
                .join(hashtag).on(studyGroupTag.hashtagId.eq(hashtag.hashtagId))
                .where(
                        studyGroupTag.groupId.eq(studyGroup.groupId),
                        studyGroupTag.deletedAt.isNull(),
                        hashtag.name.lower().like(pattern).or(hashtag.normalizedName.like(pattern))
                )
                .exists();
        return titleMatch.or(hashtagMatch);
    }

    /**
     * 인증방식: group_verification_methods에 해당 method_code가 있는 그룹만.
     * (삭제되지 않은 매핑 기준)
     */
    private BooleanExpression verificationMethodIn(java.util.List<com.checkit.studyservice.entity.VerificationMethodCode> methods) {
        if (methods == null || methods.isEmpty()) return null;
        return com.querydsl.jpa.JPAExpressions
                .selectOne()
                .from(groupVerificationMethod)
                .where(
                        groupVerificationMethod.groupId.eq(studyGroup.groupId),
                        groupVerificationMethod.deletedAt.isNull(),
                        groupVerificationMethod.methodCode.in(methods)
                )
                .exists();
    }

    // ---- 상세 필터 ----
    private BooleanExpression minMembersGoe(Integer min) {
        return min != null ? studyGroup.minMembers.goe(min) : null;
    }

    private BooleanExpression maxMembersLoe(Integer max) {
        return max != null ? studyGroup.maxMembers.loe(max) : null;
    }

    private BooleanExpression startDateBetween(java.time.LocalDate from, java.time.LocalDate to) {
        if (from != null && to != null) {
            return studyGroup.startDate.between(from, to);
        }
        if (from != null) return studyGroup.startDate.goe(from);
        if (to != null) return studyGroup.startDate.loe(to);
        return null;
    }

    private BooleanExpression isIndefiniteEq(Boolean isIndefinite) {
        return isIndefinite != null && isIndefinite ? studyGroup.isIndefinite.eq(true) : null;
    }

    private BooleanExpression joinTypeEq(com.checkit.studyservice.entity.JoinType joinType) {
        return joinType != null ? studyGroup.joinType.eq(joinType) : null;
    }

    /**
     * sort 파라미터("createdAt,desc")를 QueryDSL OrderSpecifier 배열로 변환.
     */
    private OrderSpecifier<?>[] orderBy(String sort) {
        if (!StringUtils.hasText(sort)) {
            return new OrderSpecifier[]{studyGroup.createdAt.desc()};
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());
        OrderSpecifier<?> spec;
        switch (property) {
            case "createdAt":
                spec = desc ? studyGroup.createdAt.desc() : studyGroup.createdAt.asc();
                break;
            case "currentMembers":
                spec = desc ? studyGroup.currentMembers.desc() : studyGroup.currentMembers.asc();
                break;
            case "startDate":
                spec = desc ? studyGroup.startDate.desc() : studyGroup.startDate.asc();
                break;
            default:
                spec = studyGroup.createdAt.desc();
        }
        return new OrderSpecifier[]{spec};
    }
}
