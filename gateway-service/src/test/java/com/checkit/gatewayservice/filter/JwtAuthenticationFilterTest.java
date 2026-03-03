package com.checkit.gatewayservice.filter;

import com.checkit.gatewayservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

/**
 * JwtAuthenticationFilter 단위 테스트.
 * Authorization 헤더 유무·Bearer 토큰 검증·X-User-Id/X-User-Role 전달 검증.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 단위 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private org.springframework.cloud.gateway.filter.GatewayFilterChain chain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    private ServerWebExchange exchangeWithAuth(String authHeader) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/users/1")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .build();
        return MockServerWebExchange.from(request);
    }

    private ServerWebExchange exchangeWithoutAuth() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/users/1"));
    }

    @Nested
    @DisplayName("Authorization 없음 또는 Bearer 아님")
    class Unauthorized {

        @Test
        void Authorization_헤더가_없으면_401_및_chain_미호출() {
            ServerWebExchange exchange = exchangeWithoutAuth();

            Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                    .filter(exchange, chain);

            result.block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        void Bearer_접두어가_없으면_401() {
            ServerWebExchange exchange = exchangeWithAuth("Basic abc");

            Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                    .filter(exchange, chain);

            result.block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }
    }

    @Nested
    @DisplayName("Bearer 토큰 검증")
    class BearerToken {

        @Test
        void 토큰이_유효하지_않으면_401_및_chain_미호출() {
            ServerWebExchange exchange = exchangeWithAuth("Bearer invalid-token");
            when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

            Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                    .filter(exchange, chain);

            result.block();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(any());
        }

        @Test
        void 토큰이_유효하면_chain_filter에_X_User_Id_X_User_Role_전달() {
            String userId = UUID.randomUUID().toString();
            String role = "USER";
            ServerWebExchange exchange = exchangeWithAuth("Bearer valid-token");
            when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
            when(jwtTokenProvider.getUserId("valid-token")).thenReturn(userId);
            when(jwtTokenProvider.getUserRole("valid-token")).thenReturn(role);

            ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
            when(chain.filter(exchangeCaptor.capture())).thenReturn(Mono.empty());

            Mono<Void> result = filter.apply(new JwtAuthenticationFilter.Config())
                    .filter(exchange, chain);

            result.block();
            ServerWebExchange mutated = exchangeCaptor.getValue();
            assertThat(mutated.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo(userId);
            assertThat(mutated.getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo(role);
        }
    }
}
