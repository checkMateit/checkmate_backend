package com.checkit.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuditBaseEntity is a Querydsl query type for AuditBaseEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAuditBaseEntity extends EntityPathBase<AuditBaseEntity> {

    private static final long serialVersionUID = 1628118111L;

    public static final QAuditBaseEntity auditBaseEntity = new QAuditBaseEntity("auditBaseEntity");

    public final DateTimePath<java.time.OffsetDateTime> createdAt = createDateTime("createdAt", java.time.OffsetDateTime.class);

    public final ComparablePath<java.util.UUID> createdBy = createComparable("createdBy", java.util.UUID.class);

    public final DateTimePath<java.time.OffsetDateTime> deletedAt = createDateTime("deletedAt", java.time.OffsetDateTime.class);

    public final ComparablePath<java.util.UUID> deletedBy = createComparable("deletedBy", java.util.UUID.class);

    public final DateTimePath<java.time.OffsetDateTime> updatedAt = createDateTime("updatedAt", java.time.OffsetDateTime.class);

    public final ComparablePath<java.util.UUID> updatedBy = createComparable("updatedBy", java.util.UUID.class);

    public QAuditBaseEntity(String variable) {
        super(AuditBaseEntity.class, forVariable(variable));
    }

    public QAuditBaseEntity(Path<? extends AuditBaseEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuditBaseEntity(PathMetadata metadata) {
        super(AuditBaseEntity.class, metadata);
    }

}

