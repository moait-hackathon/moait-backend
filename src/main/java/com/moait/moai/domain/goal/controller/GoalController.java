package com.moait.moai.domain.goal.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.goal.dto.CurrentAmountRequestDTO;
import com.moait.moai.domain.goal.dto.CurrentAmountResponseDTO;
import com.moait.moai.domain.goal.dto.GoalCancelResponseDTO;
import com.moait.moai.domain.goal.dto.GoalOnboardingRequestDTO;
import com.moait.moai.domain.goal.dto.GoalResponseDTO;
import com.moait.moai.domain.goal.dto.GoalUpdateRequestDTO;
import com.moait.moai.domain.goal.dto.RiskProfileRecalculateResponseDTO;
import com.moait.moai.domain.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Goal", description = "공동 목표 API")
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "공동 목표 온보딩(생성)",
            description = "온보딩 5단계 제출 → investmentPeriodMonths·R·jointRiskProfileType 산출, 커플당 1개.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponseDTO>> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GoalOnboardingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "공동 목표가 생성되었습니다.", goalService.createGoal(userId, request)));
    }

    @Operation(summary = "공동 목표 조회", description = "목표 + 진척률 + 공동 위험점수 R.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GoalResponseDTO>> getMine(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getMyGoal(userId)));
    }

    @Operation(summary = "공동 목표 수정",
            description = "부분 수정. 점수 문항 또는 targetDate 변경 시 R 자동 재계산.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<GoalResponseDTO>> update(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody GoalUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "공동 목표가 수정되었습니다.", goalService.updateGoal(userId, request)));
    }

    @Operation(summary = "현재 투자금 갱신", description = "진척 금액 갱신. 목표 도달 시 ACHIEVED 자동 전환.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/me/current-amount")
    public ResponseEntity<ApiResponse<CurrentAmountResponseDTO>> updateCurrentAmount(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CurrentAmountRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "현재 투자금이 갱신되었습니다.",
                goalService.updateCurrentAmount(userId, request.currentAmount())));
    }

    @Operation(summary = "공동 위험점수 재계산",
            description = "저장된 6문항 답변으로 investmentPeriodMonths·R·jointRiskProfileType 재계산 (멱등).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/me/recalculate-risk-profile")
    public ResponseEntity<ApiResponse<RiskProfileRecalculateResponseDTO>> recalculate(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "재계산되었습니다.", goalService.recalculateRiskProfile(userId)));
    }

    @Operation(summary = "공동 목표 취소", description = "status=CANCELLED (soft delete, 이력 보존).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<GoalCancelResponseDTO>> cancel(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "공동 목표가 취소되었습니다.", goalService.cancelGoal(userId)));
    }
}
