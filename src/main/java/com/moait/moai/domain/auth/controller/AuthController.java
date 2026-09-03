package com.moait.moai.domain.auth.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.auth.dto.AuthTokenResponseDTO;
import com.moait.moai.domain.auth.dto.LoginRequestDTO;
import com.moait.moai.domain.auth.dto.SignupRequestDTO;
import com.moait.moai.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "휴대폰 중복 확인 + 필수 약관 동의 후 가입. 성공 시 토큰 발급.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> signup(
            @Valid @RequestBody SignupRequestDTO request) {
        AuthTokenResponseDTO data = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", data));
    }

    @Operation(summary = "로그인", description = "휴대폰번호 / 비밀번호 로그인. 성공 시 토큰 발급.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        AuthTokenResponseDTO data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인되었습니다.", data));
    }
}
