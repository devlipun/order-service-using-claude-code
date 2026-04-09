package com.skmcore.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/actuator/health",
        "/actuator/info",
        "/actuator/prometheus",
        "/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            // TODO: replace with JWT/OAuth2 resource-server config before going to prod
            .httpBasic(basic -> {});

        // Allow H2 console to render in an iframe (dev only)
        http.headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()));

        return http.build();
    }
}
