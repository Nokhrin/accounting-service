package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public sealed interface OperationResult permits SingleOperationResult, TransferOperationResult {
    UUID operationId();
}
