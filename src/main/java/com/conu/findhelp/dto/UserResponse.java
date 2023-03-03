package com.conu.findhelp.dto;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class UserResponse {
    Object userData;
    String token;

    public UserResponse(Object userData, String token) {
        this.userData = userData;
        this.token = token;
    }
}
