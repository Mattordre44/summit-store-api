package com.mattordre.summitstore.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "shoes")
@DiscriminatorValue(ProductType.SHOES_VALUE)
public class Shoes extends Product {

    @Column(name = "material", length = 100)
    private String material;

}
