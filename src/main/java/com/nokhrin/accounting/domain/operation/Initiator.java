package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public sealed interface Initiator permits CustomerInitiator {
    UUID id();
    String displayName();
}
