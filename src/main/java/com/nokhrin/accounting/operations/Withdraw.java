package com.nokhrin.accounting.operations;

import java.math.BigDecimal;
import java.util.UUID;

public record Withdraw(
        UUID sourceId,
        BigDecimal amount
) implements Operation {
}
