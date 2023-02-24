package com.conu.findhelp.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("patient")
@Data
public class Patient {

    @Id
    private String id;

    private String username;
    private String password;

}
