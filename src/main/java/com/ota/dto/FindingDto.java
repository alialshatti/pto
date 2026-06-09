package com.ota.dto;

public record FindingDto(
    String ruleId,
    String message,
    String severity,
    String sourceXpath
) {}
