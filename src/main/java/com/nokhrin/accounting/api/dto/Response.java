package com.nokhrin.accounting.api.dto;

public sealed interface Response permits AccountResponse, DepositResponse, WithdrawalResponse, TransferResponse {
}
