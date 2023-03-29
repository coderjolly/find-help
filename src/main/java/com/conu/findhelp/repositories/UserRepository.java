package com.conu.findhelp.repositories;

import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.models.FindHelpUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface UserRepository extends MongoRepository<FindHelpUser, String> {

    @Query("{email:'?0',password:'?1'}")
    FindHelpUser findUserByUsernameAndPassword(String username, String password);


    @Query("{email:'?0'}")
    FindHelpUser findUserByUsername(String username);

    @Query("{status:'?0',role: '?1'}")
    List<FindHelpUser> findFindHelpUserByStatusAndRole(STATUS status, String role);

    @Query("{role:'?0'}")
    List<FindHelpUser> findFindHelpUserByRole(String role);

    @Query("{id:'?0'}")
    FindHelpUser findUserById(String id);

    @Query("{'email' : { $in : ?0}}")
    List<FindHelpUser> findFindHelpUserByEmail(List<String> emailId);


    @Query("{'date' : { $gte: ?0, $lte: ?1 } }")
    List<FindHelpUser> getPatientsBetweenDate(String startDate,String endDate);


}

