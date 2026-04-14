-- ============================================================
-- Payment Service Schema (R2DBC)
-- ============================================================

-- payments
CREATE TABLE payments (
    id          BIGSERIAL    PRIMARY KEY,
    order_id    BIGINT       NOT NULL,
    member_id   BIGINT       NOT NULL,
    seller_id   BIGINT,
    payment_id  VARCHAR(255) NOT NULL,
    payment_key VARCHAR(255) NOT NULL,
    amount      INTEGER      NOT NULL CHECK (amount >= 0),
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    created_at  TIMESTAMPTZ
);

-- outbox
CREATE TABLE outbox (
    id           BIGSERIAL    PRIMARY KEY,
    topic        VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    created_at   TIMESTAMPTZ,
    published_at TIMESTAMPTZ
);