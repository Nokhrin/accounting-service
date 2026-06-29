package com.nokhrin.accounting.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotNull(message ="Initial balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance must be non-negative")
    BigDecimal initialBalance){}
