package com.conu.findhelp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManagerController {

    @GetMapping("/testApi")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.status(HttpStatus.OK).body("Api Not Working");
    }

}

