--src/main/resources/db/migration/V3__create_operations.sql
CREATE TABLE operations (
    operation_id   UUID PRIMARY KEY,
    account_id     UUID NOT NULL,
    operation_type TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT operations_operation_type_check CHECK (
        operation_type IN ('OPEN_ACCOUNT', 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'CLOSE_ACCOUNT')
    )
);