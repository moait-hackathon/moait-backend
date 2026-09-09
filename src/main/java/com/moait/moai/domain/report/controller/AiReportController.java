package com.moait.moai.domain.report.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.report.dto.AiReportResponseDTO;
import com.moait.moai.domain.report.service.AiReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Report", description = "AI 투자 합의안 리포트 API")
@RestController
@RequestMapping("/api/ai-report")
@RequiredArgsConstructor
public class AiReportController {

    private final AiReportService aiReportService;

    @Operation(summary = "내 AI 리포트 조회", description = "연결된 커플의 최신 투자 합의안 리포트를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AiReportResponseDTO>> getMyReport(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI 리포트를 조회했습니다.", aiReportService.getMyReport(userId)));
    }
}
