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
    public void save(Account account) {
        AccountJpaEntity accountEntity = accountMapper.toEntity(account);
        jpaRepository.save(accountEntity);
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return jpaRepository.findById(accountId)
                .map(accountMapper::toDomain);
    }
}
