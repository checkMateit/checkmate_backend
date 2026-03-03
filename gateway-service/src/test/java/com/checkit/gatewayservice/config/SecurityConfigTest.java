package com.checkit.gatewayservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityConfig 빈 로드 및 필터 체인 생성 검증.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SecurityConfig 테스트")
class SecurityConfigTest {

	@Autowired(required = false)
	private SecurityWebFilterChain gatewaySecurityFilterChain;

	@Test
	void gatewaySecurityFilterChain_빈이_로드된다() {
		assertThat(gatewaySecurityFilterChain).isNotNull();
	}
}
