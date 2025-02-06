package com.example.myapp.entity.exception;

public class InputInvalidException extends RuntimeException{
    public InputInvalidException(String message) {
        super(message);
    }
}
