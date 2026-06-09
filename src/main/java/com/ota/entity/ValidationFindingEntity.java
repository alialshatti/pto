package com.ota.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "schematron_validation_finding")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationFindingEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "validation_run_id", nullable = false)
    private UUID validationRunId;

    /**
     * Business rule identifier extracted from Schematron assert/@id.
     * E.g. "OM-BR-001"
     */
    @Column(name = "rule_id", length = 100)
    private String ruleId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FindingSeverity severity;

    /**
     * XPath location from SVRL schematron-output/failed-assert/@location.
     */
    @Column(name = "source_xpath", columnDefinition = "TEXT")
    private String sourceXpath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public enum FindingSeverity {
        ERROR, WARNING, INFO
    }
}
