CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    balance NUMERIC(16, 2) NOT NULL CHECK (balance >= 0),
    status VARCHAR NOT NULL,
    holder_id UUID NOT NULL,
    holder_display_name VARCHAR NOT NULL
);