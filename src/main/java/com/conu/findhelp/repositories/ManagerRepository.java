package com.conu.findhelp.repositories;

import com.conu.findhelp.models.Manager;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ManagerRepository extends MongoRepository<Manager, String> {

    @Query("{username:'?0'}")
    Manager findManagerByUsername(String username);

}

