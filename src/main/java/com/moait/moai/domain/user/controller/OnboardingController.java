package com.moait.moai.domain.user.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.user.dto.FinancialInfoRequestDTO;
import com.moait.moai.domain.user.dto.OnboardingStepResponseDTO;
import com.moait.moai.domain.user.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Onboarding", description = "온보딩 API")
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "기본 재무정보 입력", description = "연소득/총자산 저장. 다음 온보딩 단계를 반환.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/financial-info")
    public ResponseEntity<ApiResponse<OnboardingStepResponseDTO>> updateFinancialInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FinancialInfoRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "재무정보가 저장되었습니다.", onboardingService.updateFinancialInfo(userId, request)));
    }
}
