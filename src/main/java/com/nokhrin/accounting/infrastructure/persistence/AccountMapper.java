package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;

public interface AccountMapper {
    default Account toDomain(AccountJpaEntity accountJpaEntity) {
        return new Account(
                accountJpaEntity.getId(),
                accountJpaEntity.getBalance(),
                accountJpaEntity.getStatus(),
                accountJpaEntity.getCreatedAt(),
                accountJpaEntity.getModifiedAt()
        );
    }

    default AccountJpaEntity toEntity(Account account) {
        AccountJpaEntity accountJpaEntity = new AccountJpaEntity();
        accountJpaEntity.setId(account.id());
        accountJpaEntity.setBalance(account.balance());
        accountJpaEntity.setStatus(account.status());
        accountJpaEntity.setCreatedAt(account.createdAt());
        accountJpaEntity.setModifiedAt(account.modifiedAt());

        return accountJpaEntity;
    }
}
