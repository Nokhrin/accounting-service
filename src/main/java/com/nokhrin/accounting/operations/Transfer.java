package com.nokhrin.accounting.operations;

import java.math.BigDecimal;
import java.util.UUID;

public record Transfer(
        UUID sourceId,
        UUID targetId,
        BigDecimal amount
) implements Operation {
}
