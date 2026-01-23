package com.checkit.communityservice.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInquiry is a Querydsl query type for Inquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInquiry extends EntityPathBase<Inquiry> {

    private static final long serialVersionUID = -1573547786L;

    public static final QInquiry inquiry = new QInquiry("inquiry");

    public final StringPath content = createString("content");

    public final NumberPath<Long> inquiryId = createNumber("inquiryId", Long.class);

    public final StringPath status = createString("status");

    public final StringPath title = createString("title");

    public final ComparablePath<java.util.UUID> userId = createComparable("userId", java.util.UUID.class);

    public QInquiry(String variable) {
        super(Inquiry.class, forVariable(variable));
    }

    public QInquiry(Path<? extends Inquiry> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInquiry(PathMetadata metadata) {
        super(Inquiry.class, metadata);
    }

}

