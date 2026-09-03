package com.moait.moai.domain.user.service;

import com.moait.moai.domain.user.dto.UserMeResponseDTO;

public interface UserService {

    /** 인증된 사용자 본인 정보 + 온보딩 단계. */
    UserMeResponseDTO getMe(Long userId);
}
