
package com.nokhrin.accounting.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public sealed interface OperationEvent extends Event permits
        FundsDeposited,
        FundsWithdrawn,
        TransferCompleted
{
    BigDecimal amount();
    UUID operationId();
    default UUID aggregateId() {
        return operationId();
    }
}
