package com.recoded.taqadam.models.Api;

public class UnauthenticatedException extends ApiError {

    public UnauthenticatedException(int statusCode, String message) {
        super(statusCode, message);
    }
}