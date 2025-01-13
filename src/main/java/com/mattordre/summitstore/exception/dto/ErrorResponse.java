package com.mattordre.summitstore.exception.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorResponse {

    private final String message;

    private final String errorCode;

    private final String timestamp;

}
