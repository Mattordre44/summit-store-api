package com.mattordre.summitstore.image.service;

import com.mattordre.summitstore.image.exception.ImageNotFoundException;
import com.mattordre.summitstore.image.exception.StorageAccessException;
import com.mattordre.summitstore.image.model.ImageType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final S3ClientFactory s3ClientFactory;


    @Value("${image.store.url}")
    private String endpoint;

    @Value("${image.store.access.key}")
    private String accessKey;

    @Value("${image.store.secret.key}")
    private String secretKey;

    @Value("${image.store.region}")
    private String region;


    /**
     * Retrieve an image file by name directly from object storage and download the file
     * @param fileName Image file name
     * @return Image file as response bytes object
     * @throws ImageNotFoundException If the image is not found
     * @throws StorageAccessException If there is an error accessing the object storage
     */
    public ResponseBytes<GetObjectResponse> getImageFileByName(ImageType imageType, String fileName) throws ImageNotFoundException, StorageAccessException {

        // Create S3 client
        try(S3Client s3Client = s3ClientFactory.createS3Client(endpoint, accessKey, secretKey, region)) {
            // Download file from object storage
            return s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(imageType.getBucketName())
                            .key(fileName)
                            .build(),
                    ResponseTransformer.toBytes()
            );
        } catch (NoSuchKeyException e) {
            throw new ImageNotFoundException("Image not found", e);
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Error downloading image {} from object storage", fileName, e);
            throw new StorageAccessException("Error downloading image from object storage", e);
        }
    }


    /**
     * Upload an image file to object storage
     * @param file Image file
     * @param imageType Type of the image
     * @return Image fileName stored in object storage
     * @throws StorageAccessException If there is an error accessing the object storage
     */
    public String uploadImage(MultipartFile file, ImageType imageType) throws StorageAccessException {
        // Generate a unique file name
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // Create S3 client
        try(S3Client s3Client = s3ClientFactory.createS3Client(endpoint, accessKey, secretKey, region)) {
            // Upload file to S3
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(imageType.getBucketName())
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("Image {} uploaded successfully", file.getOriginalFilename());

            return fileName;
        } catch (AwsServiceException | SdkClientException | IOException e) {
            log.error("Error uploading image {} to object storage", file.getOriginalFilename(), e);
            throw new StorageAccessException("Error uploading image to object storage", e);
        }
    }


    /**
     * Check if an image file is uploaded to object storage
     * @param fileName Image file name
     * @param imageType Type of the image
     * @return True if the image file is uploaded, false otherwise
     * @throws StorageAccessException If there is an error accessing the object storage
     */
    public boolean isImageFileUploaded(String fileName, ImageType imageType) throws StorageAccessException {
        // Create S3 client
        try(S3Client s3Client = s3ClientFactory.createS3Client(endpoint, accessKey, secretKey, region)) {
            // Upload file to S3
            s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(imageType.getBucketName())
                            .key(fileName)
                            .build(),
                    ResponseTransformer.toBytes()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (AwsServiceException | SdkClientException e) {
            log.error("Error downloading image {} to object storage", fileName, e);
            throw new StorageAccessException("Error downloading image from object storage", e);
        }
    }

}
