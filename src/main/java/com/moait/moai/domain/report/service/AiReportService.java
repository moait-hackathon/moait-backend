package com.moait.moai.domain.report.service;

import com.moait.moai.domain.report.dto.AiReportResponseDTO;

public interface AiReportService {

    AiReportResponseDTO getMyReport(Long userId);
}
