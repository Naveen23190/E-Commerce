ALTER TABLE customer_orders ADD COLUMN IF NOT EXISTS subtotal NUMERIC(12, 2);
ALTER TABLE customer_orders ADD COLUMN IF NOT EXISTS shipping_fee NUMERIC(12, 2);
ALTER TABLE customer_orders ADD COLUMN IF NOT EXISTS pin_code VARCHAR(6);
ALTER TABLE customer_orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(24);
ALTER TABLE customer_orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(24);

CREATE TABLE IF NOT EXISTS product_reviews (
    id BIGSERIAL PRIMARY KEY,
    comment VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rating INTEGER NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES app_users(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    CONSTRAINT uk_product_review_customer UNIQUE (product_id, customer_id)
);

CREATE TABLE IF NOT EXISTS delivery_addresses (
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    default_address BOOLEAN NOT NULL,
    label VARCHAR(40) NOT NULL,
    pin_code VARCHAR(6) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES app_users(id)
);

CREATE INDEX IF NOT EXISTS idx_delivery_addresses_customer ON delivery_addresses(customer_id, default_address DESC, created_at DESC);

CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(190) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(140) NOT NULL,
    quantity_change INTEGER NOT NULL,
    reason VARCHAR(200) NOT NULL,
    stock_after INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventory_movements_created ON inventory_movements(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_movements_product_created ON inventory_movements(product_id, created_at DESC);

CREATE TABLE IF NOT EXISTS payment_attempts (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    gateway_order_id VARCHAR(80) NOT NULL UNIQUE,
    gateway_payment_id VARCHAR(80),
    pin_code VARCHAR(6) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    shipping_fee NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES app_users(id),
    customer_order_id BIGINT UNIQUE REFERENCES customer_orders(id)
);

CREATE INDEX IF NOT EXISTS idx_payment_attempts_customer_created ON payment_attempts(customer_id, created_at DESC);

CREATE TABLE IF NOT EXISTS payment_attempt_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(140) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    attempt_id VARCHAR(36) NOT NULL REFERENCES payment_attempts(id)
);

CREATE INDEX IF NOT EXISTS idx_payment_attempt_items_attempt ON payment_attempt_items(attempt_id);
