package com.nokhrin.bank.operations;

import java.math.BigDecimal;
import java.util.UUID;

public record Deposit(
        UUID targetId,
        BigDecimal amount
) implements Operation {
}
