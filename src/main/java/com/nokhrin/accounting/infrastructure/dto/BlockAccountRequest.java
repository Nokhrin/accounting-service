package com.nokhrin.accounting.infrastructure.dto;

import jakarta.validation.constraints.Size;

public record BlockAccountRequest(
        @Size(max = 500)
        String reason
) implements Request {
}
