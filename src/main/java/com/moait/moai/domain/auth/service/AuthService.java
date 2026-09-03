package com.moait.moai.domain.auth.service;

import com.moait.moai.domain.auth.dto.AuthTokenResponseDTO;
import com.moait.moai.domain.auth.dto.SignupRequestDTO;

public interface AuthService {

    /** 일반(LOCAL) 회원가입. 성공 시 액세스/리프레시 토큰을 발급한다. */
    AuthTokenResponseDTO signup(SignupRequestDTO request);
}
