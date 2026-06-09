package com.ota.phive;

import com.helger.commons.error.IError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.phive.api.execute.ValidationExecutionManager;
import com.helger.phive.api.executorset.IValidationExecutorSet;
import com.helger.phive.api.executorset.IValidationExecutorSetRegistry;
import com.helger.phive.api.result.ValidationResult;
import com.helger.phive.api.result.ValidationResultList;
import com.helger.phive.xml.source.IValidationSourceXML;
import com.helger.phive.xml.source.ValidationSourceXML;
import com.ota.config.PhiveEngineConfig;
import com.ota.entity.ValidationFindingEntity.FindingSeverity;
import com.ota.exception.PhiveValidationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhiveValidationService {

    private final IValidationExecutorSetRegistry<IValidationSourceXML> vesRegistry;

    public PhiveValidationService(IValidationExecutorSetRegistry<IValidationSourceXML> vesRegistry) {
        this.vesRegistry = vesRegistry;
    }

    public PhiveValidationResult validate(byte[] invoiceXmlBytes) {
        try {
            IValidationSourceXML source = ValidationSourceXML.create(invoiceXmlBytes);
            
            IValidationExecutorSet<IValidationSourceXML> ves = vesRegistry.getOfID(PhiveEngineConfig.OMAN_TDD_VESID);
            if (ves == null) {
                throw new PhiveValidationException("Oman TDD VES not found in registry", null);
            }

            ValidationExecutionManager<IValidationSourceXML> executionManager = ves.createExecutionManager();
            ValidationResultList resultList = executionManager.executeValidation(source);

            List<PhiveFinding> findings = new ArrayList<>();
            for (ValidationResult result : resultList) {
                for (IError error : result.getErrorList()) {
                    FindingSeverity severity = mapSeverity(error.getErrorLevel());
                    findings.add(new PhiveFinding(
                        error.getErrorID(),
                        error.getErrorText(java.util.Locale.US),
                        severity,
                        error.getErrorFieldName()
                    ));
                }
            }

            // In Phive 12.0.3, overallValidity is on ValidationResultList
            boolean passed = resultList.getOverallValidity().isValid();
            
            // To properly serialize SVRL or results, we would use PhiveResult, 
            // but for simplicity in this demo we'll just format a basic XML or JSON string or leave empty
            // if SVRL isn't strictly required from phive directly. 
            // However, ph-schematron natively outputs SVRL. We'll skip raw SVRL for now or just generate a placeholder
            // since the main requirement is just executing phive and extracting structured errors.
            String svrlReportXml = "<svrl-placeholder/>"; // Replace with actual SVRL if needed from PhiveJsonHelper

            return new PhiveValidationResult(passed, findings, svrlReportXml);

        } catch (Exception e) {
            throw new PhiveValidationException("Error during phive validation", e);
        }
    }

    private FindingSeverity mapSeverity(com.helger.commons.error.level.IErrorLevel errorLevel) {
        if (errorLevel.isGE(EErrorLevel.ERROR)) {
            return FindingSeverity.ERROR;
        } else if (errorLevel.isGE(EErrorLevel.WARN)) {
            return FindingSeverity.WARNING;
        }
        return FindingSeverity.INFO;
    }
}
