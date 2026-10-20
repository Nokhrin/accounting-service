package com.nokhrin.accounting.api;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.generated.model.AccountHolderResponse;
import com.nokhrin.accounting.generated.model.AccountResponse;

public final class AccountResponseMapper {
    private AccountResponseMapper() {
    }

    private static com.nokhrin.accounting.generated.model.AccountStatus mapStatus(
            com.nokhrin.accounting.domain.account.AccountStatus domainStatus
    ) {
        return com.nokhrin.accounting.generated.model.AccountStatus.fromValue(domainStatus.name());
    }

    public static AccountResponse from(Account account) {
        return new AccountResponse()
                .id(account.id())
                .balance(account.balance())
                .status(mapStatus(account.status()))
                .holder(new AccountHolderResponse()
                        .id(account.holder().id())
                        .displayName(account.holder().displayName())
                );
    }
}
