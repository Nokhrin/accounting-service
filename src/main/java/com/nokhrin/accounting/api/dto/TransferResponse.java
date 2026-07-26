package com.nokhrin.accounting.api.dto;

public record TransferResponse(
        Response sourceAccount,
        Response targetAccount
) implements Response {
}
