package com.moait.moai.domain.analysis.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.service.InvestmentAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "부부 투자 합의안 생성", description = "A·B·C, R, G를 계산하고 합의안을 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "분석 성공")
    @PostMapping("/agreements")
    public ResponseEntity<ApiResponse<InvestmentAgreementResponseDTO>> createAgreement(
            @Valid @RequestBody InvestmentAgreementRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("투자 합의안 분석이 완료되었습니다.",
                investmentAnalysisService.analyze(request)));
    }
}
