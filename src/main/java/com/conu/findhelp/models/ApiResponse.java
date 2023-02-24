package com.conu.findhelp.models;


import lombok.Data;

@Data
public class ApiResponse {
    int statusCode;
    String error;

    Object response;

    public ApiResponse(int statusCode, String error,Object response) {
        this.statusCode = statusCode;
        this.error = error;
        this.response = response;
    }
}
