package com.nokhrin.accounting.domain.operation;

import java.util.UUID;

public class DuplicateOperationException extends RuntimeException {
    public DuplicateOperationException(UUID operationId) {
        super("Operation already processed: " + operationId);
    }
}