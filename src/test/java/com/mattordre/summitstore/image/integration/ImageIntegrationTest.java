package com.mattordre.summitstore.image.integration;


import com.mattordre.summitstore.SummitStoreApiApplication;
import com.mattordre.summitstore.image.model.ImageType;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = SummitStoreApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Testcontainers
public class ImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    @ServiceConnection
    protected static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");


    @Container
    protected static final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio:latest");

    @DynamicPropertySource
    static void configureMinioProperties(DynamicPropertyRegistry registry) {
        String minioHost = MINIO_CONTAINER.getHost();
        Integer minioPort = MINIO_CONTAINER.getMappedPort(9000);
        registry.add("image.store.url", () -> "http://" + minioHost + ":" + minioPort);
        registry.add("image.store.access.key", () -> "minioadmin");
        registry.add("image.store.secret.key", () -> "minioadmin");
        registry.add("image.store.region", () -> Region.US_EAST_1);
    }


    @BeforeAll
    static void setup() {
        try(
            S3Client s3Client = S3Client.builder()
                    .endpointOverride(URI.create("http://" + MINIO_CONTAINER.getHost() + ":" + MINIO_CONTAINER.getMappedPort(9000)))
                    .forcePathStyle(true)
                    .credentialsProvider(() -> AwsBasicCredentials.create("minioadmin", "minioadmin"))
                    .region(Region.US_EAST_1)
                    .build();
        ) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(ImageType.BRAND.getBucketName()).build());
            s3Client.createBucket(CreateBucketRequest.builder().bucket(ImageType.PRODUCT.getBucketName()).build());
        }
    }


    @Test
    void uploadImage_shouldReturnFileName() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(
                multipart("/api/image")
                        .file(imageFile)
                        .param("type", ImageType.PRODUCT_VALUE)
        );

        // Assert response
        result.andExpect(status().isOk());
        result.andExpect(content().string(notNullValue()));
        result.andExpect(content().string(org.hamcrest.Matchers.matchesPattern(".*\\.png$")));
    }


    @Test
    void getImage_shouldReturnImageContent() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "existing-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );
        String fileName = mockMvc.perform(
                multipart("/api/image")
                        .file(imageFile)
                        .param("type", String.valueOf(ImageType.PRODUCT))
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        // Execute the HTTP request to retrieve the image
        ResultActions result = mockMvc.perform(
                get("/api/image/{filename}", fileName)
                        .param("type", String.valueOf(ImageType.PRODUCT))
                        .accept(MediaType.IMAGE_PNG_VALUE)
        );

        // Assert response
        result.andExpect(status().isOk());
        result.andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE));
        result.andExpect(content().bytes("test image content".getBytes()));
    }


    @Test
    void getImage_shouldReturnNotFoundWhenImageDoesNotExist() throws Exception {
        // Prepare testing data
        String nonExistingFileName = "non-existing-image.png";

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(
                get("/api/image/{filename}", nonExistingFileName)
                        .param("type", String.valueOf(ImageType.PRODUCT))
                        .accept(MediaType.IMAGE_PNG_VALUE)
        );

        // Assert response
        result.andExpect(status().isNotFound());
    }


    @Test
    void getImage_shouldReturnBadRequestWhenImageTypeDoesNotExist() throws Exception {
        // Prepare testing data
        String nonExistingFileName = "non-existing-image.png";

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(
                get("/api/image/{filename}", nonExistingFileName)
                        .param("type", "non-existing-type")
                        .accept(MediaType.IMAGE_PNG_VALUE)
        );

        // Assert response
        result.andExpect(status().isBadRequest());
    }


    @Test
    void uploadImage_shouldReturnBadRequestWhenTypeIsMissing() throws Exception {
        // Prepare testing data
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );

        // Execute the HTTP request without the "type" parameter
        ResultActions result = mockMvc.perform(
                multipart("/api/image")
                        .file(imageFile)
        );

        // Assert response
        result.andExpect(status().isBadRequest());
    }


    @Test
    void uploadImage_shouldReturnBadRequestWhenImageIsMissing() throws Exception {
        // Execute the HTTP request without the "image" file
        ResultActions result = mockMvc.perform(
                multipart("/api/image")
                        .param("type", ImageType.PRODUCT_VALUE)
        );

        // Assert response
        result.andExpect(status().isBadRequest());
    }


    @Test
    void uploadImage_shouldReturnBadRequestForInvalidFileType() throws Exception {
        // Prepare testing data with an invalid file type
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Invalid file content".getBytes()
        );

        // Execute the HTTP request
        ResultActions result = mockMvc.perform(
                multipart("/api/image")
                        .file(invalidFile)
                        .param("type", ImageType.PRODUCT_VALUE)
        );

        // Assert response
        result.andExpect(status().isBadRequest());
    }

}
