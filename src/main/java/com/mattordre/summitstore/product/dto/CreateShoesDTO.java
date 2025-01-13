package com.mattordre.summitstore.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CreateShoesDTO extends CreateProductDTO {

    @NotNull(message = "Material is required")
    @Size(max = 100, message = "Material must have up to 100 characters")
    private String material;

}
