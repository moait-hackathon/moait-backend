package com.moait.moai.domain.couple.dto;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.user.entity.User;
import java.time.LocalDateTime;

/**
 * {@code POST /couples/connect}, {@code POST /couples/requests/{id}/accept} 응답.
 * {@code status} 는 {@code WAIT}(상대 응답 대기) 또는 {@code CONNECTED}.
 */
public record CoupleConnectResponseDTO(
        Long coupleId,
        CoupleStatus status,
        CoupleMemberDTO partner,
        LocalDateTime connectedAt
) {

    public static CoupleConnectResponseDTO of(Couple couple, User partner) {
        return new CoupleConnectResponseDTO(
                couple.getId(),
                couple.getStatus(),
                CoupleMemberDTO.of(partner),
                couple.getConnectedAt());
    }
}
