package com.ota.config;

import com.helger.commons.io.resource.ClassPathResource;
import com.helger.phive.api.executorset.IValidationExecutorSetRegistry;
import com.helger.phive.api.executorset.ValidationExecutorSetRegistry;
import com.helger.phive.xml.source.IValidationSourceXML;
import com.helger.phive.xml.schematron.ValidationExecutorSchematron;
import com.helger.phive.xml.xsd.ValidationExecutorXSD;
import com.helger.ubl21.EUBL21DocumentType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhiveEngineConfig {

    public static final VESID OMAN_TDD_VESID = new VESID("om.tdd", "invoice", "1.0");

    @Bean
    public IValidationExecutorSetRegistry<IValidationSourceXML> vesRegistry() {
        ValidationExecutorSetRegistry<IValidationSourceXML> registry = new ValidationExecutorSetRegistry<>();

        // 1. UBL 2.1 XSD validation (using standard Invoice)
        ValidationExecutorXSD xsdExecutor = ValidationExecutorXSD.create(EUBL21DocumentType.INVOICE);

        // 2. Oman TDD Schematron validation
        ClassPathResource schResource = new ClassPathResource("validation/oman-tdd-rules.sch");
        if (!schResource.exists()) {
            throw new IllegalStateException("Oman TDD Schematron file not found: " + schResource.getPath());
        }
        
        // Pure schematron executor
        ValidationExecutorSchematron schematronExecutor = ValidationExecutorSchematron.createXSLT(schResource);

        // 3. Register the VES
        registry.registerValidationExecutorSet(
            ValidationExecutorSetRegistry.create(OMAN_TDD_VESID, "Oman TDD Validation", true, xsdExecutor, schematronExecutor)
        );

        return registry;
    }
}
