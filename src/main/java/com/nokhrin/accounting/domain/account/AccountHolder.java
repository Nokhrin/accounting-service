package com.nokhrin.accounting.domain.account;

import com.nokhrin.accounting.api.dto.AccountHolderResponse;

import java.util.UUID;

public record AccountHolder(
        UUID id,
        String displayName
) {
    public AccountHolderResponse toResponse() {
        return new AccountHolderResponse(this.id, this.displayName);
    }
}
