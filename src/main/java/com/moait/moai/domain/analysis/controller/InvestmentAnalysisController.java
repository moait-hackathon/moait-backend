package com.moait.moai.domain.analysis.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.service.InvestmentAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Investment Analysis", description = "부부 투자성향 및 목표 합의안 분석")
@RestController
@RequestMapping("/api/v1/investment-analyses")
@RequiredArgsConstructor
public class InvestmentAnalysisController {

    private final InvestmentAnalysisService investmentAnalysisService;

    @Operation(summary = "부부 투자 합의안 생성",
            description = "JWT 토큰 인증이 필요한 API입니다. (개발 테스트 편의를 위해 SecurityConfig에 의해 무인증 접근이 허용되어 있습니다.) 공동 목표(goalId)의 합의안을 생성하고 investment_report에 저장합니다. C는 공동 설문 6개 항목, R은 A×0.2+B×0.2+C×0.6과 상한으로 계산합니다. G는 임시 수익률 구간 규칙이며 확률 시뮬레이션이 아닙니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "분석 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 입력 누락 또는 잘못된 값")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음 또는 유효하지 않음)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공동 목표 없음")
    @PostMapping("/agreements")
    public ResponseEntity<ApiResponse<InvestmentAgreementResponseDTO>> createAgreement(
            @Valid @RequestBody InvestmentAgreementRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("투자 합의안 분석이 완료되었습니다.",
                investmentAnalysisService.analyze(request)));
    }
}
