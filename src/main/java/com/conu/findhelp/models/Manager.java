package com.conu.findhelp.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("manager")
public class Manager {

    private String username;

    private String password;

    public Manager(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }
}
