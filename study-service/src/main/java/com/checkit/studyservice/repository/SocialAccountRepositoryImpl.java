package com.checkit.studyservice.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * social_account 조회 구현 (동일 DB 사용 가정).
 * deleted_at 컬럼이 있으면 조건 추가 가능.
 */
@Repository
public class SocialAccountRepositoryImpl implements SocialAccountRepository {

    private static final String HAS_GITHUB_SQL =
            "SELECT 1 FROM social_account WHERE user_id = :userId AND UPPER(TRIM(provider)) = 'GITHUB' LIMIT 1";
    private static final String FIND_PROVIDER_USER_ID_SQL =
            "SELECT provider_user_id FROM social_account WHERE user_id = :userId AND UPPER(TRIM(provider)) = 'GITHUB' LIMIT 1";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SocialAccountRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public boolean hasGitHubLinked(UUID userId) {
        if (userId == null) return false;
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        List<Integer> rows = jdbcTemplate.query(HAS_GITHUB_SQL, params, (rs, rowNum) -> 1);
        return rows != null && !rows.isEmpty();
    }

    @Override
    public Optional<String> findGitHubProviderUserId(UUID userId) {
        if (userId == null) return Optional.empty();
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        List<String> rows = jdbcTemplate.query(FIND_PROVIDER_USER_ID_SQL, params, (rs, rowNum) -> rs.getString("provider_user_id"));
        return rows != null && !rows.isEmpty() ? Optional.ofNullable(rows.get(0)) : Optional.empty();
    }
}
