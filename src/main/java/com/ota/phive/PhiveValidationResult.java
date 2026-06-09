package com.ota.phive;

import java.util.List;

public record PhiveValidationResult(
    boolean passed,
    List<PhiveFinding> findings,
    String svrlReportXml
) {}
