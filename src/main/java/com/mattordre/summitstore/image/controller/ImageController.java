package com.mattordre.summitstore.image.controller;

import com.mattordre.summitstore.image.dto.UploadImageDTO;
import com.mattordre.summitstore.image.exception.ImageNotFoundException;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.service.ImageProcessingService;
import com.mattordre.summitstore.image.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    private final ImageProcessingService imageProcessingService;


    @GetMapping(value = "{filename}", produces = {MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getImages(@PathVariable String filename, @RequestParam("type") ImageType imageType) {
        try {
            ResponseBytes<GetObjectResponse> responseBytes = imageService.getImageFileByName(imageType, filename);
            // Dynamically determine content type
            String contentType = Files.probeContentType(Paths.get(filename));
            // Return the image content as a stream
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(responseBytes.asByteArray());
        } catch (IOException | ImageNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> uploadImage(@Valid @ModelAttribute UploadImageDTO uploadImageDTO) {
        String fileName = imageService.uploadImage(uploadImageDTO.getImage(), uploadImageDTO.getType());
        imageProcessingService.processImageBackground(fileName, uploadImageDTO.getType());
        // Return the image fileName which can be used to access the image
        return ResponseEntity.ok(fileName);
    }

}
