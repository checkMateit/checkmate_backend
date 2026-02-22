package com.checkit.common.entity;

import jakarta.persistence.*;

/**
 * QueryDSL APT가 AuditBaseEntity(@MappedSuperclass)에 대한 QAuditBaseEntity를 생성하도록 하기 위한 더미 엔티티.
 * APT는 @Entity만 처리하고 @MappedSuperclass 단독은 Q클래스를 만들지 않으므로,
 * 이 더미를 한 개 두어 상위인 AuditBaseEntity용 Q클래스가 생성되게 합니다.
 * 비즈니스 로직/테이블에서는 사용하지 않습니다.
 */
@Entity
@Table(name = "querydsl_audit_dummy")
public class QuerydslAuditDummyEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
