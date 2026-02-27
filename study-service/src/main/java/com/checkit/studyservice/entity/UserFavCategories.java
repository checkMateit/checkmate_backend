package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * users 테이블에서 선호 카테고리(fav_category_1,2,3)만 읽기 위한 엔티티.
 * 추천 스터디 API에서 사용. (온보딩 시 설정한 선호 카테고리)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavCategories {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "fav_category_1", length = 10)
    private String favCategory1;

    @Column(name = "fav_category_2", length = 10)
    private String favCategory2;

    @Column(name = "fav_category_3", length = 10)
    private String favCategory3;
}
