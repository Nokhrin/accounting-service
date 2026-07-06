package com.nokhrin.accounting.infrastructure.dto;

import java.math.BigDecimal;

public record WithdrawRequest(BigDecimal amount) {
}
