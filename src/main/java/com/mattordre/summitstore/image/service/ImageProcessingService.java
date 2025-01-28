package com.mattordre.summitstore.image.service;

import com.mattordre.summitstore.config.rabbitMQ.ImageProcessingMessage;
import com.mattordre.summitstore.config.rabbitMQ.RabbitMQSender;
import com.mattordre.summitstore.image.model.ImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageProcessingService {

    private final RabbitMQSender rabbitMQSender;

    public void processImageBackground(String fileName, ImageType imageType) {
        var message = ImageProcessingMessage.builder()
                .fileName(fileName)
                .bucketName(imageType.getBucketName())
                .build();
        rabbitMQSender.send("image.processing.background", message);
    }

}
