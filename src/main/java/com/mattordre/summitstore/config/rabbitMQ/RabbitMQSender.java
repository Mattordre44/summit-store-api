package com.mattordre.summitstore.config.rabbitMQ;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class RabbitMQSender {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQSender.class.getName());

    private final RabbitTemplate rabbitTemplate;


    public void send(String queueName, ImageProcessingMessage message) {
        rabbitTemplate.convertAndSend(queueName, message);
        log.info("Send msg: {}", message);
    }

}
