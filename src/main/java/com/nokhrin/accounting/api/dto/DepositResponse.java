package com.nokhrin.accounting.api.dto;

import com.nokhrin.accounting.domain.account.AccountHolder;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositResponse(
        UUID id,
        BigDecimal balance,
        String status,
        AccountHolder holder
) implements Response {
}
