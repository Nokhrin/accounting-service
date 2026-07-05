package com.nokhrin.accounting.infrastructure.dto;

import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt
) {
}
