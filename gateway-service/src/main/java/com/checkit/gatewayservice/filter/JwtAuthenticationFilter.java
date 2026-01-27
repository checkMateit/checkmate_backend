package com.checkit.gatewayservice.filter;

import com.checkit.gatewayservice.security.JwtTokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            System.out.println(">>>> [Filter] 요청 진입: " + exchange.getRequest().getURI());
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println(">>>> [Filter] 헤더 누락 또는 형식 불일치");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            if (!jwtTokenProvider.validateToken(token)) {
                System.out.println(">>>> [Filter] 토큰 검증 실패");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            System.out.println(">>>> [Filter] 인증 성공! UserID: " + jwtTokenProvider.getUserId(token));

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", jwtTokenProvider.getUserId(token))
                    .header("X-User-Role", jwtTokenProvider.getUserRole(token))
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        });
    }
}
