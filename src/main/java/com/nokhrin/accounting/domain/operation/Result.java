package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public sealed interface Result permits DepositResult, TransferResult, WithdrawalResult {
    UUID operationId();
}
