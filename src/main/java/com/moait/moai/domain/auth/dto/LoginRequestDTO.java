package com.moait.moai.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank
        String phone,

        @NotBlank
        String password
) {
}
