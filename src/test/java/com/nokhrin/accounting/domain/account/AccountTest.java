package com.nokhrin.accounting.domain.account;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private final AccountHolder HOLDER = new AccountHolder(UUID.fromString("00000000-0000-0000-0000-000000000001"), "test_holder_1");

    @Test
    void open_validParams_activeAccount(){
        Account account=Account.open(new BigDecimal("100"), HOLDER);
        assertAll(
                ()->assertEquals(new BigDecimal("100"), account.balance()),
                ()->assertEquals(AccountStatus.ACTIVE, account.status()),
                ()->assertEquals(HOLDER, account.holder())
        );
    }

    @Test
    void open_negativeBalance_throwsException(){
        assertThrows(IllegalArgumentException.class,
                ()->Account.open(new BigDecimal("-1"), HOLDER));
    }

    @Test
    void block_activeAccount_transitionsToBlocked(){
        Account account=Account.open(BigDecimal.ZERO, HOLDER);
        Account accountBlocked=account.block();
        assertEquals(AccountStatus.BLOCKED, accountBlocked.status());
    }

    @Test
    void block_blockedAccount_throwsException(){
        Account accountBlocked=Account.open(BigDecimal.ZERO, HOLDER).block();
        assertThrows(IllegalStateException.class, accountBlocked::block);
    }

    @Test
    void activate_blockedAccount_transitionsToActive(){
        Account blocked=Account.open(BigDecimal.ZERO, HOLDER).block();
        Account active = blocked.activate();
        assertEquals(AccountStatus.ACTIVE, active.status());
    }

    @Test
    void close_zeroBalance_closed(){
        Account account = Account.open(BigDecimal.ZERO, HOLDER);
        Account accountClosed = account.close();
        assertEquals(AccountStatus.CLOSED, accountClosed.status());
    }

    @Test
    void close_nonZeroBalance_throwsException(){
        Account account = Account.open(BigDecimal.ONE, HOLDER);
        assertThrows(IllegalStateException.class, account::close);
    }
}