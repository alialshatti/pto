package com.ota.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValidationRequestDto(
    @NotNull(message = "tddReceiptId is mandatory") UUID tddReceiptId
) {}
