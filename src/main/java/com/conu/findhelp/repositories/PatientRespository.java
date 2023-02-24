package com.conu.findhelp.repositories;

import com.conu.findhelp.models.Manager;
import com.conu.findhelp.models.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PatientRespository extends MongoRepository<Patient,String> {

    @Query("{username:'?0',password:'?1'}")
    Manager findPatientByUsernameAndPassword(String username, String password);


    @Query("{username:'?0'}")
    Manager findPatientByUsernames(String username);
}
