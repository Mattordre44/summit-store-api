package com.mattordre.summitstore.config.rabbitMQ;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
public class ImageProcessingMessage implements Serializable {

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("bucketName")
    private String bucketName;

}
