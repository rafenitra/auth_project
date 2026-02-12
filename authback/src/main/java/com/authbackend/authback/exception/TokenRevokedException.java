package com.authbackend.authback.exception;

public class TokenRevokedException extends RuntimeException{
    public TokenRevokedException(String message) {
        super(message);
    }
}
