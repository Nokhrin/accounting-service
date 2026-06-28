package com.nokhrin.accounting.util;

import com.nokhrin.accounting.entity.Transaction;
import com.nokhrin.accounting.entity.TransactionStatus;
import com.nokhrin.accounting.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.text.CollationElementIterator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TransactionUtilsTest {

    //region Data sources

    static Stream<Arguments> provideFilterByTypeData() {
        UUID account1 = UUID.randomUUID();
        UUID account2 = UUID.randomUUID();
        Instant now = Instant.now();

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account1, now),
                createTransaction(TransactionType.WITHDRAWAL, new BigDecimal("50"), account1, null, now),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account2, now),
                createTransaction(TransactionType.TRANSFER, new BigDecimal("75"), account1, account2, now)
        );

        return Stream.of(
                arguments(transactions, TransactionType.DEPOSIT, 2),
                arguments(transactions, TransactionType.WITHDRAWAL, 1),
                arguments(transactions, TransactionType.TRANSFER, 1),
                arguments(transactions, null, 0),
                arguments(null, TransactionType.DEPOSIT, 0),
                arguments(List.of(), TransactionType.DEPOSIT, 0)
        );
    }

    static Stream<Arguments> provideFilterByDateRangeData() {
        UUID account = UUID.randomUUID();
        Instant baseTime = Instant.now();
        Instant day1 = baseTime.minus(2, ChronoUnit.DAYS);
        Instant day2 = baseTime.minus(1, ChronoUnit.DAYS);
        Instant day3 = baseTime;

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account, day1),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account, day2),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("300"), null, account, day3)
        );

        return Stream.of(
                arguments(transactions, day1, day3, 3),
                arguments(transactions, day2, day3, 2),
                arguments(transactions, day1, day2, 2),
                arguments(transactions, day2, day2, 1),
                arguments(null, day1, day3, 0),
                arguments(transactions, null, day3, 0)
        );
    }

    static Stream<Arguments> provideSumByTypeData() {
        UUID account = UUID.randomUUID();
        Instant now = Instant.now();

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account, now),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account, now),
                createTransaction(TransactionType.WITHDRAWAL, new BigDecimal("50"), account, null, now),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("150"), null, account, now)
        );

        return Stream.of(
                arguments(transactions, TransactionType.DEPOSIT, new BigDecimal("450")),
                arguments(transactions, TransactionType.WITHDRAWAL, new BigDecimal("50")),
                arguments(transactions, TransactionType.TRANSFER, BigDecimal.ZERO),
                arguments(null, TransactionType.DEPOSIT, BigDecimal.ZERO),
                arguments(transactions, null, BigDecimal.ZERO),
                arguments(List.of(), TransactionType.DEPOSIT, BigDecimal.ZERO)
        );
    }

    static Stream<Arguments> provideHasTypeData(){
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();
        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, accountId, now),
                createTransaction(TransactionType.WITHDRAWAL, new BigDecimal("50"), accountId, null, now),
                createTransaction(TransactionType.TRANSFER, new BigDecimal("50"), accountId, UUID.randomUUID(), now)
        );
        return Stream.of(
                arguments(transactions, TransactionType.DEPOSIT, true),
                arguments(transactions, TransactionType.WITHDRAWAL, true),
                arguments(transactions, TransactionType.TRANSFER, true),
                arguments(transactions, null, false),
                arguments(List.of(), TransactionType.DEPOSIT, false),
                arguments(null, TransactionType.DEPOSIT, false)
        );
    }

    //endregion

    //region Tests

    @ParameterizedTest
    @MethodSource("provideFilterByTypeData")
    void filterByType_collection_filteredByType(
            List<Transaction> transactions,
            TransactionType type,
            int expectedSize
    ) {
        List<Transaction> result = TransactionUtils.filterByType(transactions, type);
        assertEquals(expectedSize, result.size());

        if (type != null && transactions != null) {
            assertTrue(result.stream().allMatch(t -> t.type() == type));
        }
    }

    @ParameterizedTest
    @MethodSource("provideFilterByDateRangeData")
    void filterByDateRange_collection_filteredByRange(
            List<Transaction> transactions,
            Instant dateFrom,
            Instant dateTo,
            int expectedSize
    ) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            assertThrows(IllegalArgumentException.class,
                    () -> TransactionUtils.filterByDateRange(transactions, dateFrom, dateTo));
        } else {
            List<Transaction> result = TransactionUtils.filterByDateRange(transactions, dateFrom, dateTo);
            assertEquals(expectedSize, result.size());
        }
    }

    @Test
    void groupByAccount_collection_groupedByPrimaryAccount() {
        UUID account1 = UUID.randomUUID();
        UUID account2 = UUID.randomUUID();
        Instant now = Instant.now();

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account1, now),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account1, now),
                createTransaction(TransactionType.WITHDRAWAL, new BigDecimal("300"), account2, null, now)
        );

        Map<UUID, List<Transaction>> grouped = TransactionUtils.groupByAccount(transactions);

        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get(account1).size());
        assertEquals(1, grouped.get(account2).size());
    }

    @ParameterizedTest
    @MethodSource("provideSumByTypeData")
    void sumByType_collection_correctSum(
            List<Transaction> transactions,
            TransactionType type,
            BigDecimal expectedSum
    ) {
        BigDecimal result = TransactionUtils.sumByType(transactions, type);
        assertEquals(expectedSum, result);
    }

    @ParameterizedTest
    @MethodSource("provideHasTypeData")
    public void hasType_collection_containsType_expectedResult(
            Collection<Transaction> transactions,
            TransactionType type,
            boolean expected
    ){
        assertEquals(expected, TransactionUtils.hasType(transactions, type));
    }

    @Test
    void findEarliest_collection_returnsOldestTransaction() {
        UUID account = UUID.randomUUID();
        Instant now = Instant.now();

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account, now.minus(2, ChronoUnit.DAYS)),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account, now.minus(1, ChronoUnit.DAYS)),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("300"), null, account, now)
        );

        Optional<Transaction> result = TransactionUtils.findEarliest(transactions);

        assertTrue(result.isPresent());
        assertEquals(now.minus(2, ChronoUnit.DAYS), result.get().executionTimestamp());
    }

    @Test
    void findLatest_collection_returnsNewestTransaction() {
        UUID account = UUID.randomUUID();
        Instant now = Instant.now();

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account, now.minus(2, ChronoUnit.DAYS)),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account, now.minus(1, ChronoUnit.DAYS)),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("300"), null, account, now)
        );

        Optional<Transaction> result = TransactionUtils.findLatest(transactions);

        assertTrue(result.isPresent());
        assertEquals(now, result.get().executionTimestamp());
    }

    @Test
    void findMaxByAmount_collection_returnsFirstEncountered() {
        UUID account = UUID.randomUUID();
        Instant now = Instant.now();
        BigDecimal expected = new BigDecimal("300");

        List<Transaction> transactions = List.of(
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("100"), null, account, now),
                createTransaction(TransactionType.DEPOSIT, new BigDecimal("200"), null, account, now),
                createTransaction(TransactionType.DEPOSIT, expected, null, account, now)
        );

        Optional<Transaction> result = TransactionUtils.findMaxByAmount(transactions);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get().amount());
    }
    //endregion

    //region Helpers

    private static Transaction createTransaction(
            TransactionType type,
            BigDecimal amount,
            UUID sourceAccount,
            UUID targetAccount,
            Instant timestamp
    ) {
        return new Transaction(
                UUID.randomUUID(),
                type,
                amount,
                sourceAccount,
                targetAccount,
                timestamp,
                TransactionStatus.COMPLETED
        );
    }

    //endregion
}