package com.conu.findhelp.models;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("manager")
@Data
public class Manager {

    private String username;

    private String password;

    public Manager(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }
}
