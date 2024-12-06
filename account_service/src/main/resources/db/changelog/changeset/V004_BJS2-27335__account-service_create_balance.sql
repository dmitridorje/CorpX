CREATE TABLE IF NOT EXISTS balance
(
    id                 BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    account_id         BIGINT                                   NOT NULL,
    authorized_balance NUMERIC(18, 2) DEFAULT 0.00              NOT NULL,
    actual_balance     NUMERIC(18, 2) DEFAULT 0.00              NOT NULL,
    created_at         TIMESTAMPTZ    DEFAULT current_timestamp NOT NULL,
    updated_at         TIMESTAMPTZ    DEFAULT current_timestamp NOT NULL,
    version            BIGINT                                   NOT NULL,

    CONSTRAINT fk_account_id FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_balance_account_id ON balance (account_id);

COMMENT ON TABLE balance IS 'Таблица для хранения баланса с авторизационными и фактическими значениями';
COMMENT ON COLUMN balance.id IS 'Первичный ключ для таблицы balance';
COMMENT ON COLUMN balance.account_id IS 'Ссылка на таблицу account, каскадное удаление';
COMMENT ON COLUMN balance.authorized_balance IS 'Текущий авторизационный баланс';
COMMENT ON COLUMN balance.actual_balance IS 'Текущий фактический баланс';
COMMENT ON COLUMN balance.created_at IS 'Временная метка создания записи';
COMMENT ON COLUMN balance.updated_at IS 'Временная метка последнего обновления записи';
COMMENT ON COLUMN balance.version IS 'Версия записи для контроля версий';