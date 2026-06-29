package com.nokhrin.accounting.dto;

import com.nokhrin.accounting.entity.Account;
import com.nokhrin.accounting.entity.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt
) {
    public static AccountResponse fromEntity(Account account){
        return new AccountResponse(
                account.getId(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}
