package com.nokhrin.accounting.infrastructure.dto;

import jakarta.validation.constraints.Size;

public record ActivateAccountRequest(
        @Size(max = 500)
        String reason
) implements Request {
}
