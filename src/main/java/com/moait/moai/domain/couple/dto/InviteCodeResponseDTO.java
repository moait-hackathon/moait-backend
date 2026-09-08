package com.moait.moai.domain.couple.dto;

public record InviteCodeResponseDTO(String inviteCode, String shareUrl) {

    public static InviteCodeResponseDTO of(String inviteCode, String shareUrl) {
        return new InviteCodeResponseDTO(inviteCode, shareUrl);
    }
}
