package com.nokhrin.accounting.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal initialBalance
) implements Request {
}
