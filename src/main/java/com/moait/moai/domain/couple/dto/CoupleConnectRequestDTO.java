package com.moait.moai.domain.couple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(example = """
        { "inviteCode": "8F3K2Q" }""")
public record CoupleConnectRequestDTO(

        @NotBlank
        @Size(min = 6, max = 6, message = "초대 코드는 6자리입니다.")
        String inviteCode
) {
}
