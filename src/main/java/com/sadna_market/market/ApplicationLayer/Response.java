package com.sadna_market.market.ApplicationLayer;

public class Response {
    // Every function used in Service Layer will return a Response object
    // Each Response object is either a success or an error
    private boolean error;
    // if error is true, json will be null
    // if error is false, json will contain the response
    private String json;
    // if error is true, errorMessage will contain the error message
    // if error is false, errorMessage will be null
    private String errorMessage;


    // Private constructors
    private Response(String json) {
        this.json = json;
        this.error = false;
        this.errorMessage = null;
    }

    private Response(String errorMessage, boolean error) {
        this.errorMessage = errorMessage;
        this.error = true;
        this.json = null;
    }

    // Static factory methods
    public static Response success(String json) {
        return new Response(json);
    }

    public static Response error(String errorMessage) {
        return new Response(errorMessage, true);
    }

    // Getters
    public String getJson() {
        return json;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return error;
    }
}
