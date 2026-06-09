package com.ota.phive;

import com.helger.io.resource.ClassPathResource;
import com.helger.schematron.sch.SchematronResourceSCH;
import com.helger.schematron.svrl.SVRLFailedAssert;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.svrl.SVRLMarshaller;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.ubl21.UBL21Marshaller;
import com.helger.diagnostics.error.level.EErrorLevel;
import com.helger.diagnostics.error.level.IErrorLevel;
import com.ota.entity.ValidationFindingEntity.FindingSeverity;
import com.ota.exception.PhiveValidationException;
import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhiveValidationService {

    public PhiveValidationService() {
        // No-arg constructor
    }

    public PhiveValidationResult validate(final byte[] invoiceXmlBytes) {
        final List<PhiveFinding> findings = new ArrayList<>();
        String svrlReportXml = "<svrl-placeholder/>";

        // 1. XSD Schema Validation via JAXP & ph-ubl21 Schema
        try {
            final Schema schema = UBL21Marshaller.invoice().getSchema();
            final Validator validator = schema.newValidator();

            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(final SAXParseException exception) {
                    findings.add(new PhiveFinding(
                            "XSD-WARNING",
                            exception.getMessage() + " (Line " + exception.getLineNumber() + ", Column " + exception.getColumnNumber() + ")",
                            FindingSeverity.WARNING,
                            null
                    ));
                }

                @Override
                public void error(final SAXParseException exception) {
                    findings.add(new PhiveFinding(
                            "XSD-ERROR",
                            exception.getMessage() + " (Line " + exception.getLineNumber() + ", Column " + exception.getColumnNumber() + ")",
                            FindingSeverity.ERROR,
                            null
                    ));
                }

                @Override
                public void fatalError(final SAXParseException exception) {
                    // Handled via catch block or below
                }
            });

            try {
                validator.validate(new StreamSource(new ByteArrayInputStream(invoiceXmlBytes)));
            } catch (final SAXParseException e) {
                final boolean alreadyAdded = findings.stream().anyMatch(f -> f.message().contains("Line " + e.getLineNumber()));
                if (!alreadyAdded) {
                    findings.add(new PhiveFinding(
                            "XSD-FATAL",
                            e.getMessage() + " (Line " + e.getLineNumber() + ", Column " + e.getColumnNumber() + ")",
                            FindingSeverity.ERROR,
                            null
                    ));
                }
            }
        } catch (final Exception e) {
            findings.add(new PhiveFinding(
                    "XSD-SYSTEM-ERROR",
                    "Could not execute XSD validation: " + e.getMessage(),
                    FindingSeverity.ERROR,
                    null
            ));
        }

        // 2. Schematron Validation via ph-schematron-xslt (only if we did not have system errors or fatal malformed XML)
        final boolean hasFatalXmlError = findings.stream().anyMatch(f -> "XSD-FATAL".equals(f.ruleId()) || "XSD-SYSTEM-ERROR".equals(f.ruleId()));
        if (!hasFatalXmlError) {
            try {
                final ClassPathResource schResource = new ClassPathResource("validation/oman-tdd-rules.sch");
                if (!schResource.exists()) {
                    throw new IllegalStateException("Oman TDD Schematron file not found under classpath:validation/oman-tdd-rules.sch");
                }

                final SchematronResourceSCH schematron = new SchematronResourceSCH(schResource);
                if (!schematron.isValidSchematron()) {
                    findings.add(new PhiveFinding(
                            "SCH-SYSTEM-ERROR",
                            "Oman TDD Schematron schema is invalid and cannot be processed.",
                            FindingSeverity.ERROR,
                            null
                    ));
                } else {
                    final SchematronOutputType output = schematron.applySchematronValidationToSVRL(
                            new StreamSource(new ByteArrayInputStream(invoiceXmlBytes))
                    );

                    if (output != null) {
                        // Serialize SVRL output to XML string
                        final SVRLMarshaller marshaller = new SVRLMarshaller();
                        svrlReportXml = marshaller.getAsString(output);

                        // Extract failed assertions
                        final List<SVRLFailedAssert> failedAsserts = SVRLHelper.getAllFailedAssertions(output);
                        for (final SVRLFailedAssert failedAssert : failedAsserts) {
                            final FindingSeverity severity = mapSeverity(failedAssert.getFlag());

                            findings.add(new PhiveFinding(
                                    failedAssert.getID(),
                                    failedAssert.getText(),
                                    severity,
                                    failedAssert.getLocation()
                            ));
                        }
                    }
                }
            } catch (final Exception e) {
                findings.add(new PhiveFinding(
                        "SCH-SYSTEM-ERROR",
                        "Could not execute Schematron validation: " + e.getMessage(),
                        FindingSeverity.ERROR,
                        null
                ));
            }
        }

        final boolean passed = findings.stream().noneMatch(f -> f.severity() == FindingSeverity.ERROR);

        return new PhiveValidationResult(passed, findings, svrlReportXml);
    }

    private FindingSeverity mapSeverity(final IErrorLevel errorLevel) {
        if (errorLevel == null) {
            return FindingSeverity.ERROR;
        }
        if (errorLevel.isGE(EErrorLevel.ERROR)) {
            return FindingSeverity.ERROR;
        } else if (errorLevel.isGE(EErrorLevel.WARN)) {
            return FindingSeverity.WARNING;
        }
        return FindingSeverity.INFO;
    }
}
