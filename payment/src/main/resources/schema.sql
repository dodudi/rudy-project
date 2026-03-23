CREATE TABLE IF NOT EXISTS payments (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    BIGINT       NOT NULL,
    payment_id  VARCHAR(100) NOT NULL,
    payment_key VARCHAR(200) NOT NULL,
    amount      INT          NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    DEFAULT now()
);

CREATE TABLE IF NOT EXISTS outbox (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topic        VARCHAR(100) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP    DEFAULT now(),
    published_at TIMESTAMP
);
