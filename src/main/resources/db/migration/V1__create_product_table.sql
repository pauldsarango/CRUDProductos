CREATE TABLE product (
                         id          BIGINT GENERATED ALWAYS AS IDENTITY,
                         name        VARCHAR(100)  NOT NULL,
                         price       NUMERIC(10,2) NOT NULL,
                         created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         sku         VARCHAR(100)  NOT NULL,
                         version     BIGINT,
                         CONSTRAINT productos_pkey PRIMARY KEY (id),
                         CONSTRAINT uk_product_sku UNIQUE (sku)
);