package com.nokhrin.accounting.domain.event;

import java.util.UUID;

public sealed interface AccountEvent extends Event permits
        AccountOpened,
        AccountBlocked,
        AccountActivated,
        AccountClosed
{
    UUID accountId();
    default UUID aggregateId() {
        return accountId();
    }
}
