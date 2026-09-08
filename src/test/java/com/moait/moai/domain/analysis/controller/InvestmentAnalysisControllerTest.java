package com.moait.moai.domain.analysis.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.moait.moai.common.security.JwtTokenProvider;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.exception.InvestmentAnalysisDataException;
import com.moait.moai.domain.analysis.service.InvestmentAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InvestmentAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvestmentAnalysisControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InvestmentAnalysisService service;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @Test
    void acceptsOnlyUserIdAsRequiredInput() throws Exception {
        mvc.perform(post("/api/v1/investment-analyses/agreements")
                .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":101}"))
                .andExpect(status().isOk());
        verify(service).analyze(new InvestmentAgreementRequestDTO(101L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"userId\":null}", "{\"userId\":0}", "{\"userId\":-1}", "{\"goalId\":301}"})
    void rejectsMissingOrInvalidUserId(String json) throws Exception {
        mvc.perform(post("/api/v1/investment-analyses/agreements")
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void missingDatabaseInputsReturnActionableError() throws Exception {
        when(service.analyze(new InvestmentAgreementRequestDTO(101L)))
                .thenThrow(new InvestmentAnalysisDataException("개인 B: 자산을 등록해 주세요."));
        mvc.perform(post("/api/v1/investment-analyses/agreements")
                .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":101}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("개인 B: 자산을 등록해 주세요."));
    }
}
