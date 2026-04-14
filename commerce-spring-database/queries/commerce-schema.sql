-- ============================================================
-- Commerce Service Schema
-- ============================================================

-- members
CREATE TABLE  members (
    id         BIGSERIAL    PRIMARY KEY,
    username   VARCHAR(20)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- products
CREATE TABLE products (
    id          BIGSERIAL    PRIMARY KEY,
    member_id   BIGINT       NOT NULL REFERENCES members (id),
    name        VARCHAR(20)  UNIQUE,
    description VARCHAR(500),
    price       INTEGER      CHECK (price >= 0 AND price <= 100000000),
    stock       INTEGER      CHECK (stock >= 0 AND stock <= 10000000),
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);

-- orders
CREATE TABLE orders (
    id         BIGSERIAL   PRIMARY KEY,
    member_id  BIGINT      NOT NULL REFERENCES members (id),
    seller_id  BIGINT,
    status     VARCHAR(20) CHECK (status IN ('CREATED', 'COMPLETED', 'CANCELLED', 'FAILED', 'REFUNDED')),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- order_items
CREATE TABLE order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT    NOT NULL REFERENCES orders (id),
    product_id BIGINT    NOT NULL REFERENCES products (id),
    price      INTEGER   NOT NULL CHECK (price >= 0),
    quantity   INTEGER   NOT NULL CHECK (quantity >= 1),
    amount     INTEGER   NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- carts
CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    member_id  BIGINT    NOT NULL REFERENCES members (id),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- cart_items
CREATE TABLE cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT    NOT NULL REFERENCES carts (id),
    product_id BIGINT    NOT NULL REFERENCES products (id),
    quantity   INTEGER   NOT NULL CHECK (quantity >= 1),
    price      INTEGER   NOT NULL CHECK (price >= 0),
    amount     INTEGER   NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- wallets
CREATE TABLE wallets (
    id         BIGSERIAL PRIMARY KEY,
    member_id  BIGINT    NOT NULL REFERENCES members (id),
    balance    BIGINT    NOT NULL CHECK (balance >= 0),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);