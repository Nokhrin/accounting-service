package com.nokhrin.accounting.infrastructure.dto;

import jakarta.validation.constraints.Size;

public record CloseAccountRequest(
        @Size(max = 500)
        String reason
) {
}
