package com.mattordre.summitstore.product.service;

import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.repository.BrandRepository;
import com.mattordre.summitstore.exception.InvalidArgumentException;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.model.ProductVariantImage;
import com.mattordre.summitstore.image.service.ImageService;
import com.mattordre.summitstore.product.dto.CreateProductVariantImageDTO;
import com.mattordre.summitstore.product.dto.CreateShoesDTO;
import com.mattordre.summitstore.product.dto.CreateVariantDTO;
import com.mattordre.summitstore.product.model.Shoes;
import com.mattordre.summitstore.product.model.Variant;
import com.mattordre.summitstore.product.repository.ShoesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShoesService {

    private final ShoesRepository shoesRepository;

    private final BrandRepository brandRepository;

    private final ImageService imageService;


    public Shoes getShoesById(UUID id) throws NoSuchElementException {
        return shoesRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Shoes createShoes(CreateShoesDTO createShoesDTO) throws InvalidArgumentException {
        // Retrieve brand according to brandId from the dto
        Brand brand = brandRepository.findById(createShoesDTO.getBrandId()).orElseThrow(() -> new InvalidArgumentException("Brand not found"));

        Shoes shoes = Shoes.builder()
                .name(createShoesDTO.getName())
                .brand(brand)
                .price(createShoesDTO.getPrice())
                .description(createShoesDTO.getDescription())
                .material(createShoesDTO.getMaterial())
                .build();

        var variants = new ArrayList<Variant>();

        for (CreateVariantDTO variantDTO : createShoesDTO.getVariants()) {
            Variant variant = Variant.builder().variantName(variantDTO.getVariantName()).product(shoes).build();

            var images = new ArrayList<ProductVariantImage>();

            for (CreateProductVariantImageDTO imageDTO : variantDTO.getImages()) {
                // Check if image is uploaded to the object storage
                var isImageUploaded = imageService.isImageFileUploaded(imageDTO.getFileName(), ImageType.PRODUCT);

                if (!isImageUploaded) {
                    throw new InvalidArgumentException("Image not found");
                }

                var productVariantImage = ProductVariantImage.builder()
                        .fileName(imageDTO.getFileName())
                        .order(imageDTO.getOrder())
                        .bucketName(ImageType.PRODUCT.getBucketName())
                        .variant(variant)
                        .build();

                images.add(productVariantImage);
            }
            variant.setImages(images);
            variants.add(variant);
        }

        shoes.setVariants(variants);

        return shoesRepository.save(shoes);
        //return createdShoes.getId();
    }

}
