package com.mattordre.summitstore.brand.service;

import com.mattordre.summitstore.brand.dto.CreateBrandDTO;
import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.repository.BrandRepository;
import com.mattordre.summitstore.exception.InvalidArgumentException;
import com.mattordre.summitstore.image.model.BrandLogo;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.repository.BrandLogoRepository;
import com.mattordre.summitstore.image.service.ImageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BrandService {

    private final BrandRepository brandRepository;

    private final ImageService imageService;

    private final BrandLogoRepository brandLogoRepository;


    /**
     * Get all brands stored in the database
     * @return List of brands stored in the database
     */
    public List<Brand> getBrands() {
        return brandRepository.findAll();
    }


    /**
     * Get a brand by its id
     * @param id ID of the brand
     * @return Brand with the given id
     * @throws NoSuchElementException If the brand with the given id does not exist
     */
    public Brand getBrandById(Integer id) throws NoSuchElementException {
        return brandRepository.findById(id).orElseThrow();
    }


    /**
     * Get a brand by its name
     * @param name Name of the brand
     * @return Optional containing the brand with the given name, or empty if it does not exist
     */
    public Optional<Brand> getBrandByName(String name) {
        return brandRepository.findByName(name);
    }


    /**
     * Create a new brand
     * @param createBrandDTO DTO with the information to create the brand
     * @return The created brand
     */
    @Transactional
    public Brand createBrand(CreateBrandDTO createBrandDTO) throws InvalidArgumentException {
        // Check if the image file is uploaded to the object storage
        var isImageUploaded = imageService.isImageFileUploaded(createBrandDTO.getImageFileName(), ImageType.BRAND);

        if (!isImageUploaded) {
            throw new InvalidArgumentException("Image file " + createBrandDTO.getImageFileName() + " is not uploaded");
        }

        // Reference the image in the database
        BrandLogo image = brandLogoRepository.save(
                BrandLogo.builder()
                        .fileName(createBrandDTO.getImageFileName())
                        .bucketName(ImageType.BRAND.getBucketName())
                        .build()
        );

        // Create the brand in the database
        return brandRepository.save(
                Brand.builder()
                        .name(createBrandDTO.getName())
                        .description(createBrandDTO.getDescription())
                        .logo(image)
                        .build()
        );
    }

}
