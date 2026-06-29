CREATE TABLE transactions (
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
type VARCHAR(20) NOT NULL,
amount NUMERIC(16,2) NOT NULL CHECK (amount>0),
source_account_id UUID,
target_account_id UUID,
execution_timestamp TIMESTAMPTZ NOT NULL,
status VARCHAR(20) NOT NULL
);

CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_target_account ON transactions(target_account_id);
CREATE INDEX idx_transactions_execution_timestamp on transactions(execution_timestamp);