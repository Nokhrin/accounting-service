package com.nokhrin.accounting.domain.account;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    //region Eq Classes
    @Test
    void create_withNullBalance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Account.create(null));
    }

    @Test
    void create_withNegativeBalance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> Account.create(BigDecimal.valueOf(-0.01)));
    }
    //endregion EC

    //region Boundaries
    @Test
    void create_withZeroBalance_success() {
        Account account = Account.create(BigDecimal.ZERO);
        assertAll(
                () -> assertEquals(BigDecimal.ZERO, account.balance()),
                () -> assertEquals(AccountStatus.ACTIVE, account.status())
        );
    }

    @Test
    void create_withHighPrecisionBalance_success() {
        BigDecimal highPrecisionBalance = new BigDecimal("99999999999999.99");
        Account account = Account.create(highPrecisionBalance);
        assertAll(
                () -> assertEquals(highPrecisionBalance, account.balance()),
                () -> assertEquals(AccountStatus.ACTIVE, account.status())
        );
    }
    //endregion Boundaries

    //region State transitions
    @Test
    void close_activeAccountWithZeroBalance_closed() {
        Account account = Account.create(BigDecimal.ZERO);
        Account closed = account.close();
        assertEquals(AccountStatus.CLOSED, closed.status());
    }

    @Test
    void close_activeAccountWithPositiveBalance_throwIllegalStateException() {
        Account account = Account.create(new BigDecimal("123.45"));
        assertThrows(IllegalStateException.class, account::close);
    }

    @Test
    void block_activeAccount_blocked() {
        Account active = Account.create(new BigDecimal("123.45"));
        Account blocked = active.block();
        assertAll(
                () -> assertEquals(AccountStatus.BLOCKED, blocked.status()),
                () -> assertEquals(active.balance(), blocked.balance()),
                () -> assertEquals(active.id(), blocked.id())
        );
    }

    @Test
    void activate_blockedAccount_active() {
        Account blocked = Account.create(new BigDecimal("123.45")).block();
        Account active = blocked.activate();
        assertAll(
                () -> assertEquals(AccountStatus.ACTIVE, active.status()),
                () -> assertEquals(blocked.balance(), active.balance()),
                () -> assertEquals(blocked.id(), active.id())
        );
    }

    @Test
    void controlFlow_blockAlreadyBlockedAccount_throwsIllegalStateException() {
        Account blocked = Account.create(new BigDecimal("1000")).block();

        assertThrows(IllegalStateException.class, blocked::block);
    }

    @Test
    void controlFlow_activateActiveAccount_throwsIllegalStateException() {
        Account active = Account.create(new BigDecimal("1000"));

        assertThrows(IllegalStateException.class, active::activate);
    }
    //endregion
}