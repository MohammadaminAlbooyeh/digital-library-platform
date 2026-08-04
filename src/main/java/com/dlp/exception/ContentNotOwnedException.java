package com.dlp.exception;

public class ContentNotOwnedException extends RuntimeException {

    public ContentNotOwnedException(String message) {
        super(message);
    }
}

