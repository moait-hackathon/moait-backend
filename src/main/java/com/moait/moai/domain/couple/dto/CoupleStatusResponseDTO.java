package com.moait.moai.domain.couple.dto;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.domain.user.entity.User;

/**
 * {@code GET /couples/status} 배열 요소.
 * {@code status} 는 {@code WAIT}(내가 요청, 대기) / {@code REQUESTED}(상대가 요청, 내 수락 대기) / {@code CONNECTED}.
 */
public record CoupleStatusResponseDTO(
        Long coupleId,
        CoupleStatus status,
        CoupleMemberDTO partner
) {

    public static CoupleStatusResponseDTO of(Long coupleId, CoupleStatus status, User partner) {
        return new CoupleStatusResponseDTO(coupleId, status, CoupleMemberDTO.of(partner));
    }
}
