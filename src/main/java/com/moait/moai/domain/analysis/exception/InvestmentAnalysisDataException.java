package com.moait.moai.domain.analysis.exception;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;

public class InvestmentAnalysisDataException extends BusinessException {
    public InvestmentAnalysisDataException(String message) {
        super(ErrorCode.ANALYSIS_DATA_INCOMPLETE, message);
    }
}
