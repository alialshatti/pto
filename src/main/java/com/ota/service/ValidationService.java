package com.ota.service;

import com.ota.dto.ValidationResponseDto;
import com.ota.entity.TddReceiptEntity;
import com.ota.entity.TddReceiptEntity.ReceiptStatus;
import com.ota.entity.ValidationFindingEntity;
import com.ota.entity.ValidationRunEntity;
import com.ota.entity.ValidationRunEntity.ValidationStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidationService {

    private final TddReceiptRepository tddReceiptRepository;
    private final ValidationRunRepository validationRunRepository;
    private final ValidationFindingRepository validationFindingRepository;
    private final MinioStorageService storageService;
    private final HashVerificationService hashVerificationService;
    private final PhiveValidationService phiveValidationService;
    private final ValidationMapper validationMapper;

    public ValidationService(
        TddReceiptRepository tddReceiptRepository,
        ValidationRunRepository validationRunRepository,
        ValidationFindingRepository validationFindingRepository,
        MinioStorageService storageService,
        HashVerificationService hashVerificationService,
        PhiveValidationService phiveValidationService,
        ValidationMapper validationMapper
    ) {
        this.tddReceiptRepository = tddReceiptRepository;
        this.validationRunRepository = validationRunRepository;
        this.validationFindingRepository = validationFindingRepository;
        this.storageService = storageService;
        this.hashVerificationService = hashVerificationService;
        this.phiveValidationService = phiveValidationService;
        this.validationMapper = validationMapper;
    }

    @Transactional
    public ValidationResponseDto validate(UUID tddReceiptId) {
        // 1. Load receipt
        TddReceiptEntity receipt = tddReceiptRepository.findById(tddReceiptId)
            .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found for id: " + tddReceiptId));

        // 2. Update status to VALIDATING
        tddReceiptRepository.updateStatus(tddReceiptId, ReceiptStatus.VALIDATING);

        // 3. Create Validation Run
        final ValidationRunEntity initialRun = ValidationRunEntity.builder()
            .tddReceiptId(tddReceiptId)
            .validationStatus(ValidationStatus.IN_PROGRESS)
            .build();
        final ValidationRunEntity run = validationRunRepository.save(initialRun);

        try {
            // 4. Download XML
            byte[] xmlBytes = storageService.downloadXml(receipt.getBucketName(), receipt.getObjectName());

            // 5. Verify Hash
            if (!hashVerificationService.verify(xmlBytes, receipt.getSha256Hash())) {
                updateRunStatus(run, ValidationStatus.HASH_MISMATCH);
                tddReceiptRepository.updateStatus(tddReceiptId, ReceiptStatus.VALIDATION_FAILED);
                throw new HashMismatchException("SHA-256 hash verification failed for receipt: " + tddReceiptId);
            }

            // 6. Call Phive Engine
            PhiveValidationResult phiveResult = phiveValidationService.validate(xmlBytes);

            // 7. Persist findings
            List<ValidationFindingEntity> findingEntities = phiveResult.findings().stream()
                .map(validationMapper::toEntity)
                .peek(e -> e.setValidationRunId(run.getId()))
                .collect(Collectors.toList());
            validationFindingRepository.saveAll(findingEntities);

            // 8. Upload SVRL report
            String reportObjectName = storageService.uploadSvrl(run.getId().toString(), phiveResult.svrlReportXml());

            // 9. Update Run
            run.setValidationStatus(phiveResult.passed() ? ValidationStatus.PASSED : ValidationStatus.FAILED);
            run.setFindingCount(findingEntities.size());
            run.setErrorCount((int) findingEntities.stream().filter(f -> f.getSeverity() == ValidationFindingEntity.FindingSeverity.ERROR).count());
            run.setWarningCount((int) findingEntities.stream().filter(f -> f.getSeverity() == ValidationFindingEntity.FindingSeverity.WARNING).count());
            run.setReportObjectName(reportObjectName);
            run.setCompletedAt(Instant.now());
            validationRunRepository.save(run);

            // 10. Update Receipt
            tddReceiptRepository.updateStatus(tddReceiptId, phiveResult.passed() ? ReceiptStatus.VALIDATED : ReceiptStatus.VALIDATION_FAILED);

            // Map response
            return new ValidationResponseDto(
                run.getId(),
                run.getValidationStatus(),
                findingEntities.stream().map(validationMapper::toDto).collect(Collectors.toList())
            );

        } catch (HashMismatchException e) {
            throw e;
        } catch (Exception e) {
            updateRunStatus(run, ValidationStatus.STORAGE_ERROR); // Or generic FAILED
            tddReceiptRepository.updateStatus(tddReceiptId, ReceiptStatus.VALIDATION_FAILED);
            throw e;
        }
    }

    private void updateRunStatus(ValidationRunEntity run, ValidationStatus status) {
        run.setValidationStatus(status);
        run.setCompletedAt(Instant.now());
        validationRunRepository.save(run);
    }
}
