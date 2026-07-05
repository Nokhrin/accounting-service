package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.infrastructure.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository jpaRepository;
    private final AccountMapper accountMapper;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountMapper accountMapper) {
        this.jpaRepository = jpaRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity accountEntity = accountMapper.toEntity(account);
        AccountJpaEntity accountEntitySaved = jpaRepository.save(accountEntity);
        return accountMapper.toDomain(accountEntitySaved);
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return jpaRepository.findById(accountId)
                .map(accountMapper::toDomain);
    }
}
