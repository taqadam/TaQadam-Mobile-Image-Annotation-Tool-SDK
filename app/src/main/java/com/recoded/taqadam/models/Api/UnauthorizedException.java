package com.recoded.taqadam.models.Api;

public class UnauthorizedException extends ApiError {

    public UnauthorizedException(int statusCode, String message) {
        super(statusCode, message);
    }
}
