package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public record CustomerInitiator(
        UUID id,
        String displayName
) implements Initiator {
}
