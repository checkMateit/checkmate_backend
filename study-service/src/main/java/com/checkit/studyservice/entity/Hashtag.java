package com.checkit.studyservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "hashtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Hashtag extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long hashtagId;

    @Column(nullable = false, length = 30, unique = true)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 30, unique = true)
    private String normalizedName;

    @Column(name = "use_cnt", nullable = false)
    private Integer useCnt;
}
