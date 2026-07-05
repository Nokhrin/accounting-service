package com.nokhrin.accounting.infrastructure.dto;

import java.math.BigDecimal;

public record BalanceResponse(BigDecimal balance) {
    }
