package com.checkit.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QQuerydslAuditDummyEntity is a Querydsl query type for QuerydslAuditDummyEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QQuerydslAuditDummyEntity extends EntityPathBase<QuerydslAuditDummyEntity> {

    private static final long serialVersionUID = -148620331L;

    public static final QQuerydslAuditDummyEntity querydslAuditDummyEntity = new QQuerydslAuditDummyEntity("querydslAuditDummyEntity");

    public final QAuditBaseEntity _super = new QAuditBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.OffsetDateTime> createdAt = _super.createdAt;

    //inherited
    public final ComparablePath<java.util.UUID> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.OffsetDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final ComparablePath<java.util.UUID> deletedBy = _super.deletedBy;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.OffsetDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final ComparablePath<java.util.UUID> updatedBy = _super.updatedBy;

    public QQuerydslAuditDummyEntity(String variable) {
        super(QuerydslAuditDummyEntity.class, forVariable(variable));
    }

    public QQuerydslAuditDummyEntity(Path<? extends QuerydslAuditDummyEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QQuerydslAuditDummyEntity(PathMetadata metadata) {
        super(QuerydslAuditDummyEntity.class, metadata);
    }

}

