package com.mattordre.summitstore.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class CreateProductDTO {

    @NotEmpty(message = "Name is required")
    @Size(max = 100, message = "Name must have up to 100 characters")
    private String name;

    @NotNull(message = "Brand ID is required")
    @Positive(message = "Brand ID must be a positive number")
    private Integer brandId;

    @Size(max = 1000, message = "Description must have up to 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be a positive number")
    @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 digits and 2 decimals")
    private BigDecimal price;

    @NotEmpty(message = "At least one variant is required.")
    private List<CreateVariantDTO> variants;

}
