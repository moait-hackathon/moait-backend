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
            description = "userId로 연결된 커플의 공동 목표와 두 사람의 활성 보유 자산을 DB에서 조회합니다. A(male_id)·B(female_id)는 평가금액 가중 위험점수, C는 저장된 공동 설문 6개 항목입니다. R은 A×0.2+B×0.2+C×0.6에 공동 설문 상한을 적용하며 결과를 investment_report에 저장합니다. 개인 상한은 추정하지 않아 null입니다. G는 임시 수익률 구간이며 달성 확률이 아닙니다. 개발 테스트 설정은 무인증 접근을 허용합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "분석 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 입력 누락 또는 잘못된 값")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음 또는 유효하지 않음)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공동 목표 없음")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "온보딩 미완료, 분석 데이터 부족 또는 지원하지 않는 자산 데이터")
    @PostMapping("/agreements")
    public ResponseEntity<ApiResponse<InvestmentAgreementResponseDTO>> createAgreement(
            @Valid @RequestBody InvestmentAgreementRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("투자 합의안 분석이 완료되었습니다.",
                investmentAnalysisService.analyze(request)));
    }
}
