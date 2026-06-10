package com.nokhrin.bank.operations;

import java.math.BigDecimal;
import java.util.UUID;

public record Transfer(
        UUID sourceId,
        UUID targetId,
        BigDecimal amount
) implements Operation {
}
