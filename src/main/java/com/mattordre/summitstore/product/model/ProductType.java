package com.mattordre.summitstore.product.model;

public enum ProductType {

    SHOES("shoes");

    // Define the values for the ProductType enum for DiscriminatorValue that is evaluated at compile time
    public static final String SHOES_VALUE = "SHOES";

    private final String type;

    ProductType(String type) {
        this.type = type;
    }
}
