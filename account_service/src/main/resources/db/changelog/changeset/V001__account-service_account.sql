CREATE TABLE account
(
    id         bigint PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    number     varchar(64) UNIQUE NOT NULL,
    project_id bigint,
    user_id    bigint,
    type       varchar(64)        NOT NULL,
    currency   varchar(64)        NOT NULL,
    status     varchar(64)        NOT NULL,
    created_at timestamptz                 DEFAULT current_timestamp,
    updated_at timestamptz                 DEFAULT current_timestamp,
    closed_at  timestamptz,
    version    bigint                      DEFAULT 1,

    CONSTRAINT fk_project_id FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_account_user_id ON account (user_id);

CREATE OR REPLACE FUNCTION generate_random_number()
RETURNS text AS $$
DECLARE
first_digit int;
    remaining_digits int;
BEGIN
    first_digit := floor(random() * 9) + 1;
    remaining_digits := trunc(random() * (20 - 12 + 1) + 12) - 1;
RETURN concat(first_digit,
              (SELECT string_agg(floor(random() * 10)::int::text, '')
               FROM generate_series(1, remaining_digits)));
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_account_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.number IS NULL THEN
        NEW.number := generate_random_number();
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER account_number_trigger
    BEFORE INSERT ON account
    FOR EACH ROW
    EXECUTE FUNCTION set_account_number();

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := current_timestamp;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_account_updated_at
    BEFORE UPDATE ON account
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();