package com.moait.moai.domain.couple.service;

import com.moait.moai.domain.couple.dto.CoupleConnectResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleDisconnectResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleMeResponseDTO;
import com.moait.moai.domain.couple.dto.CoupleStatusResponseDTO;
import com.moait.moai.domain.couple.dto.InviteCodeResponseDTO;
import java.util.List;

public interface CoupleService {

    /** 가입 시 발급된 내 초대 코드 + 공유 URL. */
    InviteCodeResponseDTO getInviteCode(Long userId);

    /** 상대 코드 입력 → 요청(WAIT) 생성, 상대가 이미 내 코드를 입력해뒀으면 즉시 CONNECTED. */
    CoupleConnectResponseDTO connect(Long userId, String inviteCode);

    /** 나에게 온 요청(REQUESTED) 중 지정 상대의 요청을 수락 → CONNECTED. */
    CoupleConnectResponseDTO accept(Long userId, Long partnerUserId);

    /** 내가 관여된 연결 건 목록 (WAIT / REQUESTED / CONNECTED, 동시 다수 가능). */
    List<CoupleStatusResponseDTO> getStatuses(Long userId);

    /** CONNECTED 커플 + 파트너 + 온보딩 진행 상태. */
    CoupleMeResponseDTO getMe(Long userId);

    /** CONNECTED 커플을 DISCONNECTED 로 전환 (soft, 이력 보존). */
    CoupleDisconnectResponseDTO disconnect(Long userId);
}
