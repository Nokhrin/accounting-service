package com.nokhrin.accounting.util;

import com.nokhrin.accounting.entity.Transaction;
import com.nokhrin.accounting.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransactionUtils {

    private TransactionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Transaction> filterByType(
            Collection<Transaction> transactions, TransactionType type) {
        if (transactions == null || type == null) {
            return List.of();
        }
        return transactions.stream()
                .filter(transaction -> transaction.type() == type)
                .toList();
        }

    public static List<Transaction> filterByDateRange(
            Collection<Transaction> transactions, Instant dateFrom, Instant dateTo) {
        if (transactions==null || dateFrom==null || dateTo==null){
            return List.of();
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("Date From: " + dateFrom + " must be before of equal to Date To: "+ dateTo);
        }
        return transactions.stream()
                .filter(transaction -> {
                    Instant ts = transaction.executionTimestamp();
                    return !ts.isBefore(dateFrom) && !ts.isAfter(dateTo);
                })
                .toList();
    }

    public static Map<UUID, List<Transaction>> groupByAccount(
            Collection<Transaction> transactions
    ){
        if (transactions==null) return Map.of();
        return transactions.stream()
                .collect(Collectors.groupingBy(transaction ->
                switch (transaction.type()){
                    case TransactionType.DEPOSIT -> transaction.targetAccountId();
                    case TransactionType.WITHDRAWAL,
                         TransactionType.TRANSFER -> transaction.sourceAccountId();
                }));
    }

    public static BigDecimal sumByType(Collection<Transaction> transactions, TransactionType type) {
        if (transactions == null || type == null){
            return BigDecimal.ZERO;
        }
        return transactions.stream()
                .filter(transaction -> transaction.type() == type)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}
