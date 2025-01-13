CREATE TABLE image
(
    bucket_name VARCHAR(50)  NOT NULL,
    file_name   VARCHAR(100) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    image_order INTEGER,
    variant_id  UUID,
    CONSTRAINT pk_image PRIMARY KEY (file_name)
);