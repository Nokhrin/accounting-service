package com.nokhrin.accounting.domain.operation;

public sealed interface Initiator permits CustomerInitiator, SystemInitiator {
    String id();
}
