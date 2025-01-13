package com.mattordre.summitstore.image.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mattordre.summitstore.brand.model.Brand;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
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
@Entity(name = "brand_logo")
@DiscriminatorValue(ImageType.BRAND_VALUE)
public class BrandLogo extends Image {

    @JsonIgnore
    @OneToOne(mappedBy = "logo")
    private Brand brand;

}
