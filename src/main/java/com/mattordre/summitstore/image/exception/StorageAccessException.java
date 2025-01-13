package com.mattordre.summitstore.image.exception;

public class StorageAccessException extends RuntimeException {

    public StorageAccessException(String message) {
        super(message);
    }

    public StorageAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
