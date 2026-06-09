package com.ota.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "schematron_validation_run")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRunEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tdd_receipt_id", nullable = false)
    private UUID tddReceiptId;

    @Column(name = "validation_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;

    @Column(name = "finding_count", nullable = false)
    private int findingCount;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    @Column(name = "warning_count", nullable = false)
    private int warningCount;

    /**
     * Path in MinIO where the SVRL report is stored.
     * Format: validation-reports/{runId}.svrl.xml
     */
    @Column(name = "report_object_name", length = 500)
    private String reportObjectName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (validationStatus == null) {
            validationStatus = ValidationStatus.IN_PROGRESS;
        }
    }

    public enum ValidationStatus {
        IN_PROGRESS, PASSED, FAILED, HASH_MISMATCH, STORAGE_ERROR
    }
}
