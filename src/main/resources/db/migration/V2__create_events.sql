CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload JSONB NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    recorded_at TIMESTAMP NOT NULL
);