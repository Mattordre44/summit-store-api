package com.mattordre.summitstore.image.dto;

import com.mattordre.summitstore.image.model.ImageType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadImageDTO {

    @NotNull(message = "Image file is required")
    private MultipartFile image;

    @NotNull(message = "Image type is required")
    private ImageType type;

    @AssertTrue(message = "Image file must be a valid image type (PNG, JPEG, or JPG)")
    public boolean isFileTypeValid() {
        if (image == null || image.isEmpty()) {
            return false; // Validation will catch null/empty cases with a more specific error
        }

        String contentType = image.getContentType();
        return contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/jpg");
    }

    @AssertTrue(message = "Image file size must not exceed 5 MB")
    public boolean isFileSizeValid() {
        return image != null && image.getSize() <= 5 * 1024 * 1024; // 5 MB
    }

}
