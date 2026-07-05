package com.nokhrin.accounting.infrastructure.dto;

import java.time.Instant;

public record ErrorResponse(
        String description,
        Instant timestamp
) {
}
