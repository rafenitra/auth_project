package com.authbackend.authback.exception;

public class TokenExpiredException extends RuntimeException{
    public TokenExpiredException(String message){
        super(message);
    }
}
