package com.mattordre.summitstore.product.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateProductVariantImageDTO {

    private String fileName;

    private Integer order;

}
