package com.moait.moai.domain.home.service;

import com.moait.moai.domain.home.dto.HomeResponseDTO;

public interface HomeService {

    HomeResponseDTO getHome(Long userId);
}
