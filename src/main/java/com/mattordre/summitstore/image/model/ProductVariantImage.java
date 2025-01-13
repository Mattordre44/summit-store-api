package com.mattordre.summitstore.image.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mattordre.summitstore.product.model.Variant;
import jakarta.persistence.*;
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
@Entity(name = "product_variant_image")
@DiscriminatorValue(ImageType.PRODUCT_VALUE)
public class ProductVariantImage extends Image {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "variant_id")
    private Variant variant;

    @Column(name = "image_order")
    private int order;

}
