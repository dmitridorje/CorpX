CREATE TABLE IF NOT EXISTS url
(
    id         BIGSERIAL PRIMARY KEY,
    long_url   VARCHAR(2048) NOT NULL UNIQUE,
    hash       VARCHAR(6) NOT NULL UNIQUE,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hash
(
    id          BIGSERIAL PRIMARY KEY,
    hash_string VARCHAR(6) NOT NULL UNIQUE
);

CREATE SEQUENCE IF NOT EXISTS unique_hash_number_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE;