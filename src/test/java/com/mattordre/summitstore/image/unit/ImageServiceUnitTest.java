package com.mattordre.summitstore.image.unit;

import com.mattordre.summitstore.image.exception.ImageNotFoundException;
import com.mattordre.summitstore.image.exception.StorageAccessException;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.service.ImageService;
import com.mattordre.summitstore.config.S3ClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ImageServiceUnitTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private S3ClientFactory s3ClientFactory;

    @Mock
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getImageFileByName_shouldReturnResponseBytes_whenImageExists() {
        // Prepare testing data
        String fileName = "test-image.png";

        // Setup mocks
        when(s3ClientFactory.createS3Client(any(), any(), any(), any())).thenReturn(s3Client);
        doReturn(mock(ResponseBytes.class)).when(s3Client).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));

        // Execute method under test
        ResponseBytes<GetObjectResponse> response = imageService.getImageFileByName(ImageType.PRODUCT, fileName);

        // Assert results
        assertNotNull(response);
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }


    @Test
    void getImageFileByName_shouldThrowImageNotFoundException_whenImageDoesNotExist() {
        // Prepare testing data
        String fileName = "non-existent.png";

        // Setup mocks
        when(s3ClientFactory.createS3Client(any(), any(), any(), any())).thenReturn(s3Client);
        doThrow(NoSuchKeyException.class).when(s3Client).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));

        // Execute method under test & Assert results
        assertThrows(ImageNotFoundException.class, () -> imageService.getImageFileByName(ImageType.PRODUCT, fileName));
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }


    @Test
    void getImageFileByName_shouldThrowImageNotFoundException_whenBucketDoesNotExist() {
        // Prepare testing data
        String fileName = "test-image.png";

        // Setup mocks
        when(s3ClientFactory.createS3Client(any(), any(), any(), any())).thenReturn(s3Client);
        doThrow(NoSuchBucketException.class).when(s3Client).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));

        // Execute method under test & Assert results
        assertThrows(StorageAccessException.class, () -> imageService.getImageFileByName(ImageType.PRODUCT, fileName));
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }


    @Test
    void getImageFileByName_shouldThrowStorageAccessException_whenS3ErrorOccurs() {
        // Prepare testing data
        String fileName = "test-image.png";

        // Setup mocks
        when(s3ClientFactory.createS3Client(any(), any(), any(), any())).thenReturn(s3Client);
        doThrow(AwsServiceException.class).when(s3Client).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));

        // Execute method under test & Assert results
        assertThrows(StorageAccessException.class, () -> imageService.getImageFileByName(ImageType.PRODUCT, fileName));
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class), any(ResponseTransformer.class));
    }


    @Test
    void uploadImage_shouldReturnFileName_whenUploadSuccessful() throws IOException {
        // Prepare testing data
        String originalFileName = "test-image.png";
        UUID mockedUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        PutObjectResponse mockedResponse = PutObjectResponse.builder().eTag("mocked-etag").build();

        // Setup mocks
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockFile.getSize()).thenReturn(1024L);
        when(s3ClientFactory.createS3Client(any(), any(), any(), any())).thenReturn(s3Client);
        try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
            mockedUUIDStatic.when(UUID::randomUUID).thenReturn(mockedUUID);
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockedResponse);

            // Execute method under test
            String result = imageService.uploadImage(mockFile, ImageType.PRODUCT);

            // Assert results
            assertNotNull(result);
            assert(result.equals("00000000-0000-0000-0000-000000000000-test-image.png"));
            verify(mockFile, times(2)).getOriginalFilename();
            verify(mockFile, times(1)).getContentType();
            verify(mockFile, times(1)).getInputStream();
            verify(mockFile, times(1)).getSize();
        }
    }


    @Test
    void uploadImage_shouldThrowStorageAccessException_whenUploadFails() throws Exception {
        // Prepare testing data
        String originalFileName = "test-image.png";

        // Setup mocks
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);
        when(s3ClientFactory.createS3Client(any(), any(), any(), any())).thenReturn(s3Client);
        doThrow(AwsServiceException.class).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Execute method under test & Assert results
        assertThrows(StorageAccessException.class, () -> imageService.uploadImage(mockFile, ImageType.PRODUCT));
        verify(mockFile, times(2)).getOriginalFilename();
    }

}
