package com.mattordre.summitstore.config.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.service.BrandService;
import com.mattordre.summitstore.config.S3ClientFactory;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.service.ImageService;
import com.mattordre.summitstore.product.dto.CreateProductVariantImageDTO;
import com.mattordre.summitstore.product.dto.CreateShoesDTO;
import com.mattordre.summitstore.product.dto.CreateVariantDTO;
import com.mattordre.summitstore.product.model.ProductType;
import com.mattordre.summitstore.product.model.Shoes;
import com.mattordre.summitstore.product.service.ProductService;
import com.mattordre.summitstore.product.service.ShoesService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class DevConfig {

    private static final Logger log = LoggerFactory.getLogger(DevConfig.class);

    @Value("${image.store.url}")
    private String endpoint;

    @Value("${image.store.access.key}")
    private String accessKey;

    @Value("${image.store.secret.key}")
    private String secretKey;

    @Value("${image.store.region}")
    private String region;

    private final S3ClientFactory s3ClientFactory;

    private final DevBrandData devBrandData;

    private final BrandService brandService;

    private final ImageService imageService;

    private final ProductService productService;

    private final ShoesService shoesService;


    @PostConstruct
    @Transactional
    public void setup() {
        try (S3Client s3Client = s3ClientFactory.createS3Client(endpoint, accessKey, secretKey, region)) {
            try {
                // Ensure the bucket exists
                ensureBucketExists(s3Client, ImageType.BRAND.getBucketName());
                ensureBucketExists(s3Client, ImageType.PRODUCT.getBucketName());
                // Populate dev data
                populateBrandData();
                populateProductData();
            } catch (S3Exception e) {
                log.error("Error creating buckets & populate dev data", e);
            }
        }
    }


    /**
     * Ensure the bucket exists, if not create it
     * @param s3Client S3 client instance
     * @param bucketName Name of the bucket according to the ImageType
     * @throws S3Exception If an error occurs while creating & retrieving the bucket
     */
    private void ensureBucketExists(S3Client s3Client, String bucketName) throws S3Exception {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Bucket created: {}", bucketName);
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            log.info("Bucket already exists: {}", bucketName);
        }
    }


    /**
     * Populate brand data from the dev-data/brand directory
     */
    private void populateBrandData() {
        // Retrieve all directories in the brand directory
        Path brandDirPath = Paths.get("dev-data/brand");
        try (Stream<Path> directories = Files.list(brandDirPath)) {
            directories.filter(Files::isDirectory).forEach(devBrandData::populateBrand);
        } catch (IOException e) {
            log.error("Error reading brand directory: dev-data/brand", e);
        }
    }


    private void populateProductData() {
        // Populate product data
        Path productDirPath = Paths.get("dev-data/product");
        try (Stream<Path> directories = Files.list(productDirPath)) {
            directories.filter(Files::isDirectory).forEach(directory -> {
                try {
                    // Locate product JSON file
                    Path jsonFilePath = directory.resolve(directory.getFileName() + ".json");

                    if (Files.exists(jsonFilePath)) {
                        // Parse product JSON file
                        ObjectMapper objectMapper = new ObjectMapper();
                        var values = objectMapper.readTree(jsonFilePath.toFile());
                        String name = values.get("name").asText();
                        ProductType type = ProductType.valueOf(values.get("type").asText());

                        // Check if product already exists
                        if (productService.getProductByName(name).isPresent()) {
                            log.info("Product already exists: {}", name);
                            return;
                        }

                        // Create the product according to the type
                        switch (type) {
                            case ProductType.SHOES -> createShoesProduct(directory, values);
                        }
                    } else {
                        log.warn("Missing files for product in directory: {}", directory);
                    }
                } catch (IOException e) {
                    log.error("Error processing product directory: {}", directory, e);
                }
            });
        } catch (IOException e) {
            log.error("Error reading product directory: dev-data/product", e);
        }
    }

    private void createShoesProduct(Path directory, JsonNode values) throws IOException {
        var builder = CreateShoesDTO.builder()
                .name(values.get("name").asText())
                .description(values.get("description").asText())
                .price(values.get("price").decimalValue())
                .material(values.get("material").asText());

        List<CreateVariantDTO> variants = new ArrayList<>();
        for(JsonNode variant : values.get("variants")) {
            List<CreateProductVariantImageDTO> images = new ArrayList<>();
            for(JsonNode image: variant.get("images")) {
                // Upload image and set the filename in the DTO
                var imageDTO = processProductVariantImage(directory.resolve(variant.get("imageDirName").asText()), image);
                images.add(imageDTO);
            }
            variants.add(
                    CreateVariantDTO.builder()
                        .variantName(variant.get("variantName").asText())
                        .images(images)
                        .build()
            );
        }
        builder.variants(variants);
        Brand brand = brandService.getBrandByName(values.get("brand").asText()).orElseThrow(() -> new IllegalArgumentException("Brand not found: " + values.get("brand").asText()));
        builder.brandId(brand.getId());
        // Create the shoes using the service
        Shoes shoes = shoesService.createShoes(builder.build());
        log.info("Successfully added product: {}", shoes.getName());
    }


    private CreateProductVariantImageDTO processProductVariantImage(Path directory, JsonNode values) throws IOException {
        // Upload image and set the filename in the DTO
        MultipartFile imageFile = new DevModeMultipartFile(directory.resolve(values.get("fileName").asText()).toFile());
        String uploadedFileName = imageService.uploadImage(imageFile, ImageType.PRODUCT);
        return CreateProductVariantImageDTO.builder()
                .fileName(uploadedFileName)
                .order(values.get("order").asInt())
                .build();
    }

}
