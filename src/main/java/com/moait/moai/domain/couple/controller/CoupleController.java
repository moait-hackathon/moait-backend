package com.moait.moai.domain.couple.controller;

import com.moait.moai.common.response.ApiResponse;
import com.moait.moai.domain.couple.dto.CoupleConnectRequestDTO;
import com.moait.moai.domain.couple.dto.CoupleConnectResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleDisconnectResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleMeResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleStatusResponseDTO;
import com.moait.moai.domain.couple.dto.InviteCodeResponseDTO;
import com.moait.moai.domain.couple.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Couple", description = "부부 연결 API")
@RestController
@RequestMapping("/api/couples")
@RequiredArgsConstructor
public class CoupleController {

    private final CoupleService coupleService;

    @Operation(summary = "내 초대 코드 조회", description = "회원가입 시 자동 발급된 초대 코드 + 공유 URL.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/invite-code")
    public ResponseEntity<ApiResponse<InviteCodeResponseDTO>> inviteCode(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(coupleService.getInviteCode(userId)));
    }

    @Operation(summary = "연결 요청 / 확정",
            description = "상대 코드 입력. 상대가 아직이면 WAIT 생성, 상대가 이미 내 코드를 입력했으면 즉시 CONNECTED.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<CoupleConnectResponseDTO>> connect(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CoupleConnectRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(
                "연결 요청이 처리되었습니다.", coupleService.connect(userId, request.inviteCode())));
    }

    @Operation(summary = "연결 요청 수락", description = "나에게 온 요청(REQUESTED) 중 지정 상대의 요청을 수락 → CONNECTED.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/requests/{partnerUserId}/accept")
    public ResponseEntity<ApiResponse<CoupleConnectResponseDTO>> accept(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long partnerUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                "커플이 연결되었습니다.", coupleService.accept(userId, partnerUserId)));
    }

    @Operation(summary = "내 연결 상태 목록", description = "대기/요청/연결 상태 목록 (동시 다수 가능, DISCONNECTED 제외).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<CoupleStatusResponseDTO>>> status(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(coupleService.getStatuses(userId)));
    }

    @Operation(summary = "연결된 커플 조회", description = "CONNECTED 커플 + 파트너 정보 + 온보딩 진행 상태.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CoupleMeResponseDTO>> me(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(coupleService.getMe(userId)));
    }

    @Operation(summary = "연결 해제", description = "CONNECTED 커플을 DISCONNECTED 로 전환 (soft, 이력 보존).",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<CoupleDisconnectResponseDTO>> disconnect(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                "커플 연결이 해제되었습니다.", coupleService.disconnect(userId)));
    }
}
