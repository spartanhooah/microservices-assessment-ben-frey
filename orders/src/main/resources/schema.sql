CREATE TABLE IF NOT EXISTS customers (
                                         id UUID PRIMARY KEY,
                                         first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    street_address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip VARCHAR(255),
    phone_number VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS products (
                                        id UUID PRIMARY KEY,
                                        name VARCHAR(255),
    price NUMERIC(38, 2)
    );

CREATE TABLE IF NOT EXISTS orders (
                                      id UUID PRIMARY KEY,
                                      customer_id UUID,
                                      status VARCHAR(50),
                                      CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           order_id UUID NOT NULL,
                                           product_id UUID NOT NULL,
                                           quantity INTEGER NOT NULL,
                                           PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
    );