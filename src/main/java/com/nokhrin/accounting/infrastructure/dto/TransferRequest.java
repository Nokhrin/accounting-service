package com.nokhrin.accounting.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) implements Request {
}
