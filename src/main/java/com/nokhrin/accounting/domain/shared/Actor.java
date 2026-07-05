package com.nokhrin.accounting.domain.shared;

public sealed interface Actor permits CustomerActor, SystemActor {
    String id();
}
