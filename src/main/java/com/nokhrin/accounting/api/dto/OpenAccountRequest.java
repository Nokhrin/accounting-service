package com.nokhrin.accounting.api.dto;

import com.nokhrin.accounting.domain.account.AccountHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenAccountRequest(
        @NotNull @DecimalMin("0.00")BigDecimal initialBalance,
        @Valid @NotNull AccountHolder holder
        ) {
}
