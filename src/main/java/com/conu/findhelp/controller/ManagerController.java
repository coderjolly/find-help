package com.conu.findhelp.controller;

import com.conu.findhelp.models.Manager;
import com.conu.findhelp.repositories.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ManagerController {

    @Autowired
    ManagerRepository managerRepository;

    @GetMapping("/testApi")
    public ResponseEntity<List<Manager>> testApi() {
        return ResponseEntity.status(HttpStatus.OK).body(managerRepository.findAll());
    }

}

