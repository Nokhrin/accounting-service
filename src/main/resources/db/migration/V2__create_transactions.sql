CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(16, 2) NOT NULL,
    source_account_id UUID,
    target_account_id UUID,
    execution_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_transactions_amount CHECK (amount > 0),
    CONSTRAINT chk_transactions_type CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    CONSTRAINT chk_transactions_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_txn_source ON transactions(source_account_id);
CREATE INDEX idx_txn_target ON transactions(target_account_id);
CREATE INDEX idx_txn_timestamp ON transactions(execution_timestamp);