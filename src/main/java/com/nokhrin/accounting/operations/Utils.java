package com.nokhrin.accounting.operations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Utils {
    public static Optional<List<Operation>> filterByAccountId(
            List<Operation> operationList,
            UUID accountId
    ) {
        if (operationList == null || accountId == null) {
            return Optional.empty();
        }
        return Optional.of(operationList.stream()
                .filter(operation -> switch (operation) {
                    case Deposit deposit -> deposit.targetId().equals(accountId);
                    case Withdraw withdraw -> withdraw.sourceId().equals(accountId);
                    case Transfer transfer -> transfer.sourceId().equals(accountId)
                            || transfer.targetId().equals(accountId);
                })
                .toList());
    }


}
