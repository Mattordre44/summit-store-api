package com.mattordre.summitstore.brand.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBrandDTO {

    @NotEmpty(message = "Name is required")
    @Length(max = 100, message = "Name cannot be longer than 100 characters")
    private String name;

    @NotEmpty(message = "Description is required")
    @Length(max = 1000, message = "Description cannot be longer than 1000 characters")
    private String description;

    @NotEmpty(message = "Image file name is required")
    @Length(max = 100, message = "Image file name cannot be longer than 100 characters")
    private String imageFileName;

}
