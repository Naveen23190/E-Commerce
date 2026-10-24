CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(80) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(2000) NOT NULL,
    image_url VARCHAR(500),
    name VARCHAR(140) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock INTEGER NOT NULL
);

CREATE TABLE shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES app_users(id)
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES shopping_carts(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    CONSTRAINT uk_cart_item_product UNIQUE (cart_id, product_id)
);

CREATE TABLE customer_orders (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES app_users(id)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(140) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    order_id BIGINT NOT NULL REFERENCES customer_orders(id)
);

CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX idx_customer_orders_customer_created ON customer_orders(customer_id, created_at DESC);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
