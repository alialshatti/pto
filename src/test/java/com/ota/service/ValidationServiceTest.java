package com.ota.service;

import com.ota.dto.ValidationResponseDto;
import com.ota.entity.TddReceiptEntity;
import com.ota.entity.ValidationRunEntity;
import com.ota.exception.HashMismatchException;
import com.ota.exception.ReceiptNotFoundException;
import com.ota.mapper.ValidationMapper;
import com.ota.phive.PhiveValidationResult;
import com.ota.phive.PhiveValidationService;
import com.ota.repository.TddReceiptRepository;
import com.ota.repository.ValidationFindingRepository;
import com.ota.repository.ValidationRunRepository;
import com.ota.storage.MinioStorageService;
import com.ota.util.HashVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ValidationServiceTest {

    @Mock private TddReceiptRepository tddReceiptRepository;
    @Mock private ValidationRunRepository validationRunRepository;
    @Mock private ValidationFindingRepository validationFindingRepository;
    @Mock private MinioStorageService storageService;
    @Mock private HashVerificationService hashVerificationService;
    @Mock private PhiveValidationService phiveValidationService;
    @Mock private ValidationMapper validationMapper;

    @InjectMocks
    private ValidationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateReceiptNotFound() {
        UUID id = UUID.randomUUID();
        when(tddReceiptRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ReceiptNotFoundException.class, () -> service.validate(id));
    }

    @Test
    void testValidateHashMismatch() {
        UUID id = UUID.randomUUID();
        TddReceiptEntity receipt = TddReceiptEntity.builder()
                .id(id).bucketName("b").objectName("o").sha256Hash("hash").build();
        ValidationRunEntity run = ValidationRunEntity.builder().id(UUID.randomUUID()).build();

        when(tddReceiptRepository.findById(id)).thenReturn(Optional.of(receipt));
        when(validationRunRepository.save(any())).thenReturn(run);
        when(storageService.downloadXml("b", "o")).thenReturn(new byte[]{});
        when(hashVerificationService.verify(any(), eq("hash"))).thenReturn(false);

        assertThrows(HashMismatchException.class, () -> service.validate(id));

        verify(tddReceiptRepository).updateStatus(id, TddReceiptEntity.ReceiptStatus.VALIDATING);
        verify(tddReceiptRepository).updateStatus(id, TddReceiptEntity.ReceiptStatus.VALIDATION_FAILED);
    }

    @Test
    void testValidateSuccess() {
        UUID id = UUID.randomUUID();
        TddReceiptEntity receipt = TddReceiptEntity.builder()
                .id(id).bucketName("b").objectName("o").sha256Hash("hash").build();
        ValidationRunEntity run = ValidationRunEntity.builder().id(UUID.randomUUID()).build();
        PhiveValidationResult result = new PhiveValidationResult(true, Collections.emptyList(), "<svrl/>");

        when(tddReceiptRepository.findById(id)).thenReturn(Optional.of(receipt));
        when(validationRunRepository.save(any())).thenReturn(run);
        when(storageService.downloadXml("b", "o")).thenReturn(new byte[]{});
        when(hashVerificationService.verify(any(), eq("hash"))).thenReturn(true);
        when(phiveValidationService.validate(any())).thenReturn(result);
        when(storageService.uploadSvrl(any(), any())).thenReturn("path");

        ValidationResponseDto response = service.validate(id);

        assertNotNull(response);
        assertEquals(ValidationRunEntity.ValidationStatus.PASSED, run.getValidationStatus());
        verify(tddReceiptRepository).updateStatus(id, TddReceiptEntity.ReceiptStatus.VALIDATED);
        verify(validationFindingRepository).saveAll(any());
    }
}
