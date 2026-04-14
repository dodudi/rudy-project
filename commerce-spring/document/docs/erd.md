```mermaid
erDiagram                                                                                                                                                                                                                     members {
  bigint id PK
  varchar username UK
  varchar password
  varchar nickname
  timestamptz created_at
  timestamptz updated_at
}

products {
  bigint id PK
  bigint member_id FK
  varchar name UK
  varchar description
  int price
  int stock
  timestamptz created_at
  timestamptz updated_at
}

orders {
  bigint id PK
  bigint member_id FK
  bigint member_coupon_id FK "nullable"
  int discount_amount
  varchar status
  timestamptz created_at
  timestamptz updated_at
}

order_items {
  bigint id PK
  bigint order_id FK
  bigint product_id FK
  int price
  int quantity
  int amount
  timestamptz created_at
  timestamptz updated_at
}

wallets {
  bigint id PK
  bigint member_id FK
  bigint balance
  timestamptz created_at
  timestamptz updated_at
}

carts {
  bigint id PK
  bigint member_id FK
  timestamptz created_at
  timestamptz updated_at
}

cart_items {
  bigint id PK
  bigint cart_id FK
  bigint product_id FK
  int quantity
  int price
  int amount
  timestamptz created_at
  timestamptz updated_at
}

coupons {
  bigint id PK
  varchar name UK
  varchar discount_type
  int discount_value
  int minimum_purchase_amount
  int maximum_discount_amount
  timestamptz expires_at
  timestamptz created_at
  timestamptz updated_at
}

redeem_codes {
  bigint id PK
  bigint coupon_id FK
  varchar code UK
  varchar status "AVAILABLE, CLAIMED"
  timestamptz created_at
  timestamptz updated_at
}

member_coupons {
  bigint id PK
  bigint member_id FK
  bigint coupon_id FK
  bigint redeem_code_id FK
  varchar status "ACTIVE, USED, EXPIRED"
  timestamptz used_at "nullable"
  timestamptz created_at
  timestamptz updated_at
}

members ||--o{ products : "판매"
members ||--o{ orders : "주문"
members ||--o| wallets : "잔고"
members ||--o{ carts : "장바구니"
members ||--o{ member_coupons : "쿠폰 보유"
orders ||--o{ order_items : ""
orders }o--o| member_coupons : "쿠폰 적용"
products ||--o{ order_items : ""
products ||--o{ cart_items : ""
carts ||--o{ cart_items : ""
coupons ||--o{ redeem_codes : ""
coupons ||--o{ member_coupons : ""
redeem_codes ||--o| member_coupons : ""

```
