package com.nokhrin.accounting.infrastructure.dto;

public sealed interface Request permits
        CreateAccountRequest, BlockAccountRequest, ActivateAccountRequest,
        DepositRequest, WithdrawRequest, TransferRequest {
}
