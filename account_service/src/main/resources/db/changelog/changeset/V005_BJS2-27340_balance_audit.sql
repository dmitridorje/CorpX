CREATE TABLE balance_audit (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    account_id BIGINT NOT NULL,
    balance_version BIGINT DEFAULT 0,
    authorized_balance NUMERIC,
    actual_balance NUMERIC,
    request_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp
);

CREATE INDEX account_id_idx ON balance_audit (account_id);
