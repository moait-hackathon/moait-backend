package com.moait.moai.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final long ACCESS_VALIDITY_SECONDS = 3600L;
    private static final long REFRESH_VALIDITY_SECONDS = 1_209_600L;

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(new JwtProperties(
            "moait-test-secret-key-for-junit-only-0123456789abcdef",
            ACCESS_VALIDITY_SECONDS,
            REFRESH_VALIDITY_SECONDS));

    @Test
    @DisplayName("액세스 토큰을 생성하면 userId 를 다시 꺼낼 수 있다")
    void createAndParseAccessToken() {
        Long userId = 1001L;

        String token = tokenProvider.createAccessToken(userId);

        assertThat(tokenProvider.validate(token)).isTrue();
        assertThat(tokenProvider.getUserId(token)).isEqualTo(userId);

        // 토큰이 실제로 어떻게 생겼는지 콘솔에 출력
        printToken("ACCESS", token);
    }

    @Test
    @DisplayName("리프레시 토큰도 동일하게 동작한다")
    void createAndParseRefreshToken() {
        String token = tokenProvider.createRefreshToken(2002L);

        assertThat(tokenProvider.validate(token)).isTrue();
        assertThat(tokenProvider.getUserId(token)).isEqualTo(2002L);

        printToken("REFRESH", token);
    }

    @Test
    @DisplayName("서명이 다른 토큰은 검증에 실패한다")
    void rejectTokenSignedWithAnotherKey() {
        JwtTokenProvider attacker = new JwtTokenProvider(new JwtProperties(
                "totally-different-secret-key-0123456789abcdefghij", 3600L, 3600L));

        String forged = attacker.createAccessToken(9999L);

        assertThat(tokenProvider.validate(forged)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void rejectExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(new JwtProperties(
                "moait-test-secret-key-for-junit-only-0123456789abcdef", -1L, -1L));

        String expired = shortLived.createAccessToken(1L);

        assertThat(tokenProvider.validate(expired)).isFalse();
    }

    private void printToken(String label, String token) {
        String[] parts = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        System.out.printf("%n[%s TOKEN] %s%n", label, token);
        System.out.printf("  header : %s%n", new String(decoder.decode(parts[0])));
        System.out.printf("  payload: %s%n", new String(decoder.decode(parts[1])));
        System.out.printf("  (signature 조각은 base64 서명값 — 디코드 불필요)%n");
    }
}
