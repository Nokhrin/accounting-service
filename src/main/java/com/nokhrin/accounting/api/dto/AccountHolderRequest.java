package com.nokhrin.accounting.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountHolderRequest(
        @NotNull UUID id,
        @NotNull String displayName
        ) {
}
