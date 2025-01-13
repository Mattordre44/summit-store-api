package com.mattordre.summitstore.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mattordre.summitstore.image.model.ProductVariantImage;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "variant")
@Table(name = "variant")
public class Variant {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "variant", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ProductVariantImage> images;

}
