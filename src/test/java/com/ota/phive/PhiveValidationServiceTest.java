package com.ota.phive;

import com.helger.phive.api.executorset.IValidationExecutorSet;
import com.helger.phive.api.executorset.IValidationExecutorSetRegistry;
import com.helger.phive.api.execute.ValidationExecutionManager;
import com.helger.phive.api.result.ValidationResult;
import com.helger.phive.api.result.ValidationResultList;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.phive.api.validity.EExtendedValidity;
import com.helger.phive.xml.source.IValidationSourceXML;
import com.ota.config.PhiveEngineConfig;
import com.ota.exception.PhiveValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhiveValidationServiceTest {

    @Mock
    private IValidationExecutorSetRegistry<IValidationSourceXML> vesRegistry;

    @InjectMocks
    private PhiveValidationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateVesNotFound() {
        when(vesRegistry.getOfID(any())).thenReturn(null);
        
        // Passing valid empty XML just to parse
        assertThrows(PhiveValidationException.class, () -> service.validate("<xml/>".getBytes()));
    }
}
