package com.checkit.communityservice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInquiryComment is a Querydsl query type for InquiryComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInquiryComment extends EntityPathBase<InquiryComment> {

    private static final long serialVersionUID = 2141721737L;

    public static final QInquiryComment inquiryComment = new QInquiryComment("inquiryComment");

    public final StringPath authorType = createString("authorType");

    public final NumberPath<Long> commentId = createNumber("commentId", Long.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> inquiryId = createNumber("inquiryId", Long.class);

    public final ComparablePath<java.util.UUID> userId = createComparable("userId", java.util.UUID.class);

    public QInquiryComment(String variable) {
        super(InquiryComment.class, forVariable(variable));
    }

    public QInquiryComment(Path<? extends InquiryComment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInquiryComment(PathMetadata metadata) {
        super(InquiryComment.class, metadata);
    }

}

