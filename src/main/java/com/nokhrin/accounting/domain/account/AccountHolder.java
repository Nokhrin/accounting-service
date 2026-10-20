package com.nokhrin.accounting.domain.account;

import java.util.UUID;

public record AccountHolder(
        UUID id,
        String displayName
) {
}
