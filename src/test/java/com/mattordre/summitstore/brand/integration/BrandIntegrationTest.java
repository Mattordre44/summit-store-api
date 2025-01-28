package com.mattordre.summitstore.brand.integration;

import com.mattordre.summitstore.SummitStoreApiApplication;
import com.mattordre.summitstore.brand.dto.CreateBrandDTO;
import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.repository.BrandRepository;
import com.mattordre.summitstore.image.model.BrandLogo;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.repository.BrandLogoRepository;
import org.junit.jupiter.api.*;
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

import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
        classes = SummitStoreApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrandIntegrationTest {

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
    private BrandLogoRepository brandLogoRepository;

    @Autowired
    private BrandRepository brandRepository;


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
    @Order(1) // Ensure this test runs first to avoid conflicts with other tests
    void getBrands_shouldReturnExistingBrands() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile1 = new MockMultipartFile("image", "logo1.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        MockMultipartFile imageFile2 = new MockMultipartFile("image", "logo2.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String fileName1 = mockMvc.perform(multipart("/api/image").file(imageFile1).param("type", String.valueOf(ImageType.BRAND))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String fileName2 = mockMvc.perform(multipart("/api/image").file(imageFile2).param("type", String.valueOf(ImageType.BRAND))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        BrandLogo brandLogo1 = BrandLogo.builder().fileName(fileName1).bucketName(ImageType.BRAND.getBucketName()).build();
        BrandLogo brandLogo2 = BrandLogo.builder().fileName(fileName2).bucketName(ImageType.BRAND.getBucketName()).build();
        Brand brand1 = Brand.builder().name("Brand1").description("Description for Brand1").logo(brandLogo1).build();
        Brand brand2 = Brand.builder().name("Brand2").description("Description for Brand2").logo(brandLogo2).build();
        brandLogoRepository.saveAll(List.of(brandLogo1, brandLogo2));
        brandRepository.saveAll(List.of(brand1, brand2));

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(get("/api/brand"));

        // Assert response
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$", hasSize(2)));
        result.andExpect(jsonPath("$[0].name").value("Brand1"));
        result.andExpect(jsonPath("$[0].description").value("Description for Brand1"));
        result.andExpect(jsonPath("$[0].logo.fileName").value(fileName1));
        result.andExpect(jsonPath("$[1].name").value("Brand2"));
        result.andExpect(jsonPath("$[1].description").value("Description for Brand2"));
        result.andExpect(jsonPath("$[1].logo.fileName").value(fileName2));
    }


    @Test
    void getBrandById_shouldReturnBrandDetails() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile = new MockMultipartFile("image", "logo3.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String fileName = mockMvc.perform(multipart("/api/image").file(imageFile).param("type", String.valueOf(ImageType.BRAND))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        BrandLogo brandLogo = BrandLogo.builder().fileName(fileName).bucketName(ImageType.BRAND.getBucketName()).build();
        Brand brand = Brand.builder().name("Brand3").description("Description for Brand3").logo(brandLogo).build();
        brandLogoRepository.save(brandLogo);
        brandRepository.save(brand);

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(get("/api/brand/" + brand.getId()));

        // Assert response
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.id").value(brand.getId()));
        result.andExpect(jsonPath("$.name").value("Brand3"));
        result.andExpect(jsonPath("$.description").value("Description for Brand3"));
        result.andExpect(jsonPath("$.logo.fileName").value(fileName));
    }


    @Test
    void getBrandById_shouldReturnNotFoundForNonExistingId() throws Exception {
        // Execute the HTTP request
        ResultActions result = mockMvc.perform(get("/api/brand/999"));

        // Assert response
        result.andExpect(status().isNotFound());
    }


    @Test
    void createBrand_shouldReturnCreatedBrand() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile = new MockMultipartFile("image", "logo4.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        String fileName = mockMvc.perform(multipart("/api/image").file(imageFile).param("type", String.valueOf(ImageType.BRAND))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        CreateBrandDTO createBrandDTO = CreateBrandDTO.builder()
                .name("Brand4")
                .description("Description for Brand4")
                .imageFileName(fileName)
                .build();

        // Convert DTO to JSON
        String createBrandJson = new ObjectMapper().writeValueAsString(createBrandDTO);

        // Execute the HTTP POST request
        ResultActions result = mockMvc.perform(post("/api/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBrandJson));

        // Assert response
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.name").value("Brand4"));
        result.andExpect(jsonPath("$.description").value("Description for Brand4"));
        result.andExpect(jsonPath("$.logo.fileName").value(fileName));
    }


    @Test
    void createBrand_shouldReturnBadRequestForEmptyFields() throws Exception {
        // Prepare an invalid input DTO with empty fields
        CreateBrandDTO invalidCreateBrandDTO = CreateBrandDTO.builder()
                .name("")
                .description("")
                .imageFileName("")
                .build();

        // Convert DTO to JSON
        String invalidCreateBrandJson = new ObjectMapper().writeValueAsString(invalidCreateBrandDTO);

        // Execute the HTTP POST request
        ResultActions result = mockMvc.perform(post("/api/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCreateBrandJson));

        // Assert response
        result.andExpect(status().isBadRequest());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.name").value("Name is required"));
        result.andExpect(jsonPath("$.description").value("Description is required"));
        result.andExpect(jsonPath("$.imageFileName").value("Image file name is required"));
    }

    @Test
    void createBrand_shouldReturnBadRequestForLongName() throws Exception {
        // Prepare an invalid input DTO with a name exceeding max length
        CreateBrandDTO invalidCreateBrandDTO = CreateBrandDTO.builder()
                .name("A".repeat(101))
                .description("Valid Description")
                .imageFileName("valid-image-file.png")
                .build();

        // Convert DTO to JSON
        String invalidCreateBrandJson = new ObjectMapper().writeValueAsString(invalidCreateBrandDTO);

        // Execute the HTTP POST request
        ResultActions result = mockMvc.perform(post("/api/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCreateBrandJson));

        // Assert response
        result.andExpect(status().isBadRequest());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.name").value("Name cannot be longer than 100 characters"));
    }

    @Test
    void createBrand_shouldReturnBadRequestForLongDescription() throws Exception {
        // Prepare an invalid input DTO with a description exceeding max length
        CreateBrandDTO invalidCreateBrandDTO = CreateBrandDTO.builder()
                .name("Valid Name")
                .description("A".repeat(1001))
                .imageFileName("valid-image-file.png")
                .build();

        // Convert DTO to JSON
        String invalidCreateBrandJson = new ObjectMapper().writeValueAsString(invalidCreateBrandDTO);

        // Execute the HTTP POST request
        ResultActions result = mockMvc.perform(post("/api/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCreateBrandJson));

        // Assert response
        result.andExpect(status().isBadRequest());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.description").value("Description cannot be longer than 1000 characters"));
    }

    @Test
    void createBrand_shouldReturnBadRequestForLongImageFileName() throws Exception {
        // Prepare an invalid input DTO with an image file name exceeding max length
        CreateBrandDTO invalidCreateBrandDTO = CreateBrandDTO.builder()
                .name("Valid Name")
                .description("Valid Description")
                .imageFileName("A".repeat(101))
                .build();

        // Convert DTO to JSON
        String invalidCreateBrandJson = new ObjectMapper().writeValueAsString(invalidCreateBrandDTO);

        // Execute the HTTP POST request
        ResultActions result = mockMvc.perform(post("/api/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCreateBrandJson));

        // Assert response
        result.andExpect(status().isBadRequest());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(jsonPath("$.imageFileName").value("Image file name cannot be longer than 100 characters"));
    }


}
