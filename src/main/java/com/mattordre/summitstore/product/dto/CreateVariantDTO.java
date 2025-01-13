package com.mattordre.summitstore.product.dto;

import com.mattordre.summitstore.image.model.ProductVariantImage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateVariantDTO {

    private String variantName;

    private List<CreateProductVariantImageDTO> images;

}
