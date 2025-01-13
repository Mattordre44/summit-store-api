CREATE TABLE variant
(
    id           UUID         NOT NULL,
    product_id   UUID         NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_variant PRIMARY KEY (id),
    CONSTRAINT FK_VARIANT_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product (id)
);

ALTER TABLE image
    ADD CONSTRAINT FK_IMAGE_ON_VARIANT FOREIGN KEY (variant_id) REFERENCES variant (id);