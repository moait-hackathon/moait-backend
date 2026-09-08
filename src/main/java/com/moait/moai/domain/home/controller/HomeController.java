package com.moait.moai.domain.home.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.home.dto.HomeResponseDTO;
import com.moait.moai.domain.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "홈 화면 조회", description = "사용자·커플 목표·자산 요약과 수익률 그래프를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<HomeResponseDTO>> getHome(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(homeService.getHome(userId)));
    }
}
