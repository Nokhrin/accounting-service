package com.nokhrin.accounting.util;

import com.nokhrin.accounting.entity.Transaction;
import com.nokhrin.accounting.entity.TransactionStatus;
import com.nokhrin.accounting.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                arguments((List<Transaction>) null, TransactionType.DEPOSIT, 0),
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
                arguments((List<Transaction>) null, day1, day3, 0),
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
                arguments((List<Transaction>) null, TransactionType.DEPOSIT, BigDecimal.ZERO),
                arguments(transactions, null, BigDecimal.ZERO),
                arguments(List.of(), TransactionType.DEPOSIT, BigDecimal.ZERO)
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

    //endregion

    //region Helper methods

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