CREATE TABLE IF NOT EXISTS tariff (
    id                          bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    tariff_name                 VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS savings_account_rate (
    id          bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    tariff_id   bigint NOT NULL,
    rate        numeric NOT NULL,
    created_at  timestamptz DEFAULT current_timestamp,

    CONSTRAINT  fk_tariff_id FOREIGN KEY (tariff_id) REFERENCES tariff (id)
);

CREATE TABLE IF NOT EXISTS savings_account (
    id                          bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    account_number              varchar(64) UNIQUE,
--     account_number              varchar(64) UNIQUE NOT NULL,
    last_date_percent           timestamptz,
    version                     bigint DEFAULT 1,
    created_at                  timestamptz DEFAULT current_timestamp,
    updated_at                  timestamptz DEFAULT current_timestamp,

    CONSTRAINT fk_account_number FOREIGN KEY (account_number) REFERENCES account (number)
);

CREATE OR REPLACE FUNCTION update_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = current_timestamp;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
    BEFORE UPDATE ON savings_account
    FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS tariff_history (
    id                          bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    savings_account_id          bigint NOT NULL,
    savings_account_tariff_id      bigint NOT NULL,
    created_at                  timestamptz DEFAULT current_timestamp,

    CONSTRAINT fr_savings_account_id FOREIGN KEY (savings_account_id) REFERENCES savings_account(id),
    CONSTRAINT fr_savings_account_tariff FOREIGN KEY (savings_account_tariff_id) REFERENCES tariff(id)
);


