package com.moait.moai.domain.couple.dto;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.domain.couple.entity.Couple;

public record CoupleDisconnectResponseDTO(Long coupleId, CoupleStatus status) {

    public static CoupleDisconnectResponseDTO of(Couple couple) {
        return new CoupleDisconnectResponseDTO(couple.getId(), couple.getStatus());
    }
}
