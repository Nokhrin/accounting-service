package com.nokhrin.accounting.domain.operation;

import java.math.BigDecimal;
import java.util.UUID;

public record Transfer(
        UUID sourceId,
        UUID targetId,
        BigDecimal amount
) implements Operation {
}
