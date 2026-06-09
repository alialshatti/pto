package com.ota.controller;

import com.ota.dto.ValidationRequestDto;
import com.ota.dto.ValidationResponseDto;
import com.ota.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponseDto> validate(@Valid @RequestBody ValidationRequestDto request) {
        ValidationResponseDto response = validationService.validate(request.tddReceiptId());
        return ResponseEntity.ok(response);
    }
}
