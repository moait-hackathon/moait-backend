package com.moait.moai.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 값 (prefix: {@code jwt}).
 *
 * <ul>
 *   <li>{@code jwt.secret} — HS256 서명 키 (최소 32바이트). 운영은 환경변수 주입.</li>
 *   <li>{@code jwt.access-token-validity-seconds} — 액세스 토큰 유효기간(초)</li>
 *   <li>{@code jwt.refresh-token-validity-seconds} — 리프레시 토큰 유효기간(초)</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {
}
