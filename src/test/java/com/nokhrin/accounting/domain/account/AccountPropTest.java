package com.nokhrin.accounting.domain.account;

import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AccountPropTest {
    @Provide
    Arbitrary<BigDecimal> nonNegativeBigDecimal(){
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("1000000000000.00"))
                .ofScale(2);
    }

    private final AccountHolder HOLDER = new AccountHolder(UUID.fromString("00000000-0000-0000-0000-000000000001"), "test_holder_1");

    @Property
    void openedAccountBalanceNeverNegative(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance
    ) {
        Account account = Account.open(initialBalance, HOLDER);
        assertTrue(account.balance().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Property
    void identityPreservedAfterTransition(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance
    ) {
        Account opened = Account.open(initialBalance, HOLDER);
        Account blocked = opened.block();
        Account activated = blocked.activate();

        assertAll(
                () -> assertEquals(opened.id(), blocked.id()),
                () -> assertEquals(opened.id(), activated.id()),
                () -> assertEquals(opened.holder(), blocked.holder()),
                () -> assertEquals(opened.holder(), activated.holder())
        );
    }

    @Property
    void transitionReturnsNewInstance(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance
    ) {
        Account opened = Account.open(initialBalance, HOLDER);
        Account blocked = opened.block();
        Account activated = blocked.activate();

        assertAll(
                () -> assertNotSame(opened, blocked),
                () -> assertNotSame(opened, activated)
        );
    }

    @Property
    void closeZeroBalanceAllowed(
            @ForAll BigDecimal balance
    ){
        assumeTrue(balance.compareTo(BigDecimal.ZERO) > 0);
        Account account = Account.open(balance, HOLDER);
        assertThrows(IllegalStateException.class, account::close);
    }

    @Property
    void activateBlockedAllowed(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal balance
    ){
        Account account = Account.open(balance, HOLDER);
        assertThrows(IllegalStateException.class, account::activate);
    }
}