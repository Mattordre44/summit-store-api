package com.mattordre.summitstore.image.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {

    PRODUCT("product", "product-image"),
    BRAND("brand", "brand-image");

    // Define the values for the ImageType enum for DiscriminatorValue that is evaluated at compile time
    public static final String PRODUCT_VALUE = "PRODUCT";
    public static final String BRAND_VALUE = "BRAND";

    private final String type;

    private final String bucketName;

}
