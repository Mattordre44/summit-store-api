package com.mattordre.summitstore.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattordre.summitstore.brand.dto.CreateBrandDTO;
import com.mattordre.summitstore.brand.service.BrandService;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.service.ImageProcessingService;
import com.mattordre.summitstore.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class DevBrandData {

    private static final Logger log = LoggerFactory.getLogger(DevConfig.class);

    private final BrandService brandService;

    private final ImageService imageService;

    private final ImageProcessingService imageProcessingService;


    /**
     * Populate brand data from JSON and image files in the given directory
     * @param directory Directory containing JSON and image files
     */
    public void populateBrand(Path directory) {
        try {
            // Locate JSON and image files
            Path jsonFilePath = directory.resolve(directory.getFileName() + ".json");

            if (Files.exists(jsonFilePath)) {
                // Parse JSON file into DTO
                ObjectMapper objectMapper = new ObjectMapper();
                var values = objectMapper.readTree(jsonFilePath.toFile());

                // Check if brand already exists
                if (brandService.getBrands().stream().anyMatch(brand -> brand.getName().equals(values.get("name").asText()))) {
                    log.info("Brand already exists: {}", values.get("name").asText());
                    return;
                }

                Path logoFilePath = directory.resolve(values.get("image").asText());
                MultipartFile imageFile = new DevModeMultipartFile(logoFilePath.toFile());

                // Upload image and set the filename in the DTO
                String uploadedFileName = imageService.uploadImage(imageFile, ImageType.BRAND);
                imageProcessingService.processImageBackground(uploadedFileName, ImageType.BRAND);

                CreateBrandDTO brandDTO = CreateBrandDTO.builder()
                        .name(values.get("name").asText())
                        .description(values.get("description").asText())
                        .imageFileName(uploadedFileName)
                        .build();

                // Create brand using the service
                brandService.createBrand(brandDTO);

                log.info("Successfully added brand: {}", brandDTO.getName());
            } else {
                log.warn("Missing files for brand in directory: {}", directory);
            }
        } catch (IOException e) {
            log.error("Error processing brand directory: {}", directory, e);
        }
    }

}
