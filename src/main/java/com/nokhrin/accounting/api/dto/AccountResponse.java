package com.nokhrin.accounting.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        BigDecimal balance,
        String status,
        AccountHolderResponse holder
) implements Response {
}
