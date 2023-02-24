package com.conu.findhelp.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {

    String username;
    String token;
    String name;
    List<String> roles;

    public UserResponse(String username, String token,List<String> roles) {
        this.username = username;
        this.token = token;
        this.roles = roles;
    }
}
