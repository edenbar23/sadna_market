package com.sadna_market.market.ApplicationLayer;

import lombok.Getter;

@Getter
public class Response<T> {
    // Every function used in Service Layer will return a Response object
    // Each Response object is either a success or an error
    private final boolean error;
    // Getters
    // if error is true, json will be null
    // if error is false, json will contain the response
    private final T data;
    // if error is true, errorMessage will contain the error message
    // if error is false, errorMessage will be null
    private final String errorMessage;


    // Private constructors
    private Response(T data) {
        this.data = data;
        this.error = false;
        this.errorMessage = null;
    }

    private Response(String errorMessage, boolean error) {
        this.errorMessage = errorMessage;
        this.error = true;
        this.data = null;
    }

    // Static factory methods
    public static <T> Response<T> success(T data) {
        return new Response<>(data);
    }

    public static <T> Response<T> error(String errorMessage) {
        return new Response<>(errorMessage, true);
    }
}