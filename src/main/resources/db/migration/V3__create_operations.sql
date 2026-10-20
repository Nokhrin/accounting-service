CREATE TABLE operations (
    operation_id   UUID PRIMARY KEY,
    account_id     UUID NOT NULL,
    operation_type TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);