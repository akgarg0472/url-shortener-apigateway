package com.akgarg.us.apigw.exception;

public class ApiGatewayException extends RuntimeException{

    public ApiGatewayException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
