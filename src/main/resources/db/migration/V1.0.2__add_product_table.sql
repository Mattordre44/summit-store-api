CREATE TABLE product
(
    id          UUID           NOT NULL,
    type        VARCHAR(20)   NOT NULL,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(1000)  NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    brand_id    INTEGER        NOT NULL,
    material    VARCHAR(100),
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT uc_product_name UNIQUE (name),
    CONSTRAINT FK_PRODUCT_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand (id)
);