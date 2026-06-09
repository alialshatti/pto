package com.ota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ota.dto.ValidationRequestDto;
import com.ota.dto.ValidationResponseDto;
import com.ota.entity.ValidationRunEntity.ValidationStatus;
import com.ota.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ValidationService validationService;

    @Test
    void testValidateEndpoint() throws Exception {
        UUID receiptId = UUID.randomUUID();
        UUID runId = UUID.randomUUID();
        ValidationRequestDto request = new ValidationRequestDto(receiptId);
        ValidationResponseDto response = new ValidationResponseDto(runId, ValidationStatus.PASSED, Collections.emptyList());

        when(validationService.validate(any())).thenReturn(response);

        mockMvc.perform(post("/internal/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationRunId").value(runId.toString()))
                .andExpect(jsonPath("$.status").value("PASSED"));
    }
}
