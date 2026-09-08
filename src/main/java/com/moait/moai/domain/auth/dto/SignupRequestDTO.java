package com.moait.moai.domain.auth.dto;

import com.moait.moai.common.enums.Gender;
import com.moait.moai.common.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(example = """
        {
          "name": "홍길동",
          "phone": "01012345678",
          "password": "P@ssw0rd!",
          "gender": "MALE",
          "agreements": [
            { "termsType": "SERVICE", "agreed": true },
            { "termsType": "PRIVACY", "agreed": true },
            { "termsType": "FINANCE", "agreed": true },
            { "termsType": "MARKETING", "agreed": false }
          ]
        }""")
public record SignupRequestDTO(

        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
        String phone,

        @NotBlank
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @NotNull
        Gender gender,

        @NotEmpty
        @Valid
        List<AgreementItem> agreements
) {

    public record AgreementItem(
            @NotNull TermsType termsType,
            @NotNull Boolean agreed
    ) {
    }
}
