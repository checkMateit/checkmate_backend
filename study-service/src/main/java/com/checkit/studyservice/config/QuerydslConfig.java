package com.checkit.studyservice.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 사용을 위한 설정.
 * JPAQueryFactory를 Bean으로 등록해 두면, 원하는 곳에서 주입받아 타입 세이프한 동적 쿼리를 작성할 수 있습니다.
 */
@Configuration
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
