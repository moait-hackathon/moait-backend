package com.moait.moai.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc OpenAPI 메타 정보 및 JWT 인증 스킴.
 *
 * <p>Swagger UI 우측 상단 <b>Authorize</b> 에 액세스 토큰을 입력하면 이후 요청에
 * {@code Authorization: Bearer <token>} 헤더가 자동으로 붙는다.
 * 개별 API 에는 {@code @SecurityRequirement(name = "bearerAuth")} 로 자물쇠를 표시한다.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "MoAI API", version = "v1"),
        servers = @Server(url = "/", description = "current")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
