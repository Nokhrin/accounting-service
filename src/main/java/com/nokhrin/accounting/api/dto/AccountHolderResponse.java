package com.nokhrin.accounting.api.dto;

import java.util.UUID;

public record AccountHolderResponse(
        UUID id,
        String displayName
) {
}
