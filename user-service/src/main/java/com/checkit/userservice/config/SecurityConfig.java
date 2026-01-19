package com.checkit.userservice.config;

import com.checkit.userservice.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;

    public SecurityConfig(OAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
        System.out.println(">>>> [DEBUG] SecurityConfig 로드 성공! <<<<");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/oauth2/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)

                                // OIDC 처리
                                .oidcUserService(userRequest -> {

                                    OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

                                    return new DefaultOidcUser(
                                            oAuth2User.getAuthorities(),
                                            userRequest.getIdToken(),
                                            new OidcUserInfo(oAuth2User.getAttributes())
                                    );
                                })
                        )
                        .defaultSuccessUrl("/", true)
                );

        return http.build();
    }
}
