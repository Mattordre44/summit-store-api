package com.mattordre.summitstore.product.integration;

import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.repository.BrandRepository;
import com.mattordre.summitstore.image.model.BrandLogo;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.repository.BrandLogoRepository;
import com.mattordre.summitstore.product.dto.CreateProductVariantImageDTO;
import com.mattordre.summitstore.product.dto.CreateShoesDTO;
import com.mattordre.summitstore.product.dto.CreateVariantDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
        classes = com.mattordre.summitstore.SummitStoreApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Testcontainers
public class ProductIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    protected static final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio:latest");

    @Container
    @ServiceConnection
    protected static final RabbitMQContainer RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq:4.0-management")
            .withAdminPassword("password");

    @DynamicPropertySource
    static void configureMinioProperties(DynamicPropertyRegistry registry) {
        String minioHost = MINIO_CONTAINER.getHost();
        Integer minioPort = MINIO_CONTAINER.getMappedPort(9000);
        registry.add("image.store.url", () -> "http://" + minioHost + ":" + minioPort);
        registry.add("image.store.access.key", () -> "minioadmin");
        registry.add("image.store.secret.key", () -> "minioadmin");
        registry.add("image.store.region", () -> Region.US_EAST_1);
        String rabbitMqHost = RABBIT_MQ_CONTAINER.getHost();
        Integer rabbitMqPort = RABBIT_MQ_CONTAINER.getMappedPort(5672);
        registry.add("spring.rabbitmq.host", () -> rabbitMqHost);
        registry.add("spring.rabbitmq.port", () -> rabbitMqPort);
    }

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BrandLogoRepository brandLogoRepository;


    @BeforeAll
    static void setup() {
        try (
                S3Client s3Client = S3Client.builder()
                        .endpointOverride(URI.create("http://" + MINIO_CONTAINER.getHost() + ":" + MINIO_CONTAINER.getMappedPort(9000)))
                        .forcePathStyle(true)
                        .credentialsProvider(() -> AwsBasicCredentials.create("minioadmin", "minioadmin"))
                        .region(Region.US_EAST_1)
                        .build()
        ) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(ImageType.BRAND.getBucketName()).build());
            s3Client.createBucket(CreateBucketRequest.builder().bucket(ImageType.PRODUCT.getBucketName()).build());
        }
    }


    @Test
    void createProduct_shouldAddProductSuccessfully() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String fileName = mockMvc.perform(multipart("/api/image").file(imageFile).param("type", String.valueOf(ImageType.BRAND))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        BrandLogo logo = brandLogoRepository.save(BrandLogo.builder().fileName(fileName).bucketName("test-bucket").build());
        Brand brand = brandRepository.save(Brand.builder().name("Brand1").description("Description Brand1").logo(logo).build());
        MockMultipartFile v1File1 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v1fileName1 = mockMvc.perform(multipart("/api/image").file(v1File1).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMultipartFile v1File2 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v1fileName2 = mockMvc.perform(multipart("/api/image").file(v1File2).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMultipartFile v1File3 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v1fileName3 = mockMvc.perform(multipart("/api/image").file(v1File3).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMultipartFile v1File4 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v1fileName4 = mockMvc.perform(multipart("/api/image").file(v1File4).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var imageDtos1 = List.of(
                CreateProductVariantImageDTO.builder().fileName(v1fileName1).order(0).build(),
                CreateProductVariantImageDTO.builder().fileName(v1fileName2).order(1).build(),
                CreateProductVariantImageDTO.builder().fileName(v1fileName3).order(2).build(),
                CreateProductVariantImageDTO.builder().fileName(v1fileName4).order(3).build()
        );
        MockMultipartFile v2File1 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v2fileName1 = mockMvc.perform(multipart("/api/image").file(v2File1).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMultipartFile v2File2 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v2fileName2 = mockMvc.perform(multipart("/api/image").file(v2File2).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMultipartFile v2File3 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v2fileName3 = mockMvc.perform(multipart("/api/image").file(v2File3).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockMultipartFile v2File4 = new MockMultipartFile("image", "logo.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String v2fileName4 = mockMvc.perform(multipart("/api/image").file(v2File4).param("type", String.valueOf(ImageType.PRODUCT))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var imageDtos2 = List.of(
                CreateProductVariantImageDTO.builder().fileName(v2fileName1).order(2).build(),
                CreateProductVariantImageDTO.builder().fileName(v2fileName2).order(1).build(),
                CreateProductVariantImageDTO.builder().fileName(v2fileName3).order(0).build(),
                CreateProductVariantImageDTO.builder().fileName(v2fileName4).order(3).build()
        );
        var createVariantDTO1 = CreateVariantDTO.builder().variantName("Black").images(imageDtos1).build();
        var createVariantDTO2 = CreateVariantDTO.builder().variantName("White").images(imageDtos2).build();
        CreateShoesDTO createShoesDTO = CreateShoesDTO
                .builder()
                .name("Product1")
                .description("A high-quality hiking shoe")
                .price(BigDecimal.valueOf(99.99))
                .material("Leather")
                .brandId(brand.getId())
                .variants(List.of(createVariantDTO1, createVariantDTO2))
                .build();

        // Convert DTO to JSON
        String createJsonDTO = new ObjectMapper().writeValueAsString(createShoesDTO);

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(post("/api/product/shoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJsonDTO));

        // Assert response
        result.andExpect(status().isCreated());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.name").value("Product1"));
        result.andExpect(jsonPath("$.description").value("A high-quality hiking shoe"));
        result.andExpect(jsonPath("$.price").value(99.99));
        result.andExpect(jsonPath("$.material").value("Leather"));
        result.andExpect(jsonPath("$.brand.name").value("Brand1"));
        result.andExpect(jsonPath("$.variants", hasSize(2)));
        result.andExpect(jsonPath("$.variants[0].variantName").value("Black"));
        result.andExpect(jsonPath("$.variants[0].images", hasSize(4)));
        result.andExpect(jsonPath("$.variants[1].variantName").value("White"));
        result.andExpect(jsonPath("$.variants[1].images", hasSize(4)));
    }

}
