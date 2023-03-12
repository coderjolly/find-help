package com.conu.findhelp.controller;

import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RequestMapping("/api/v1/doctor")
@RestController
public class DoctorController {


    @Autowired
    UserRepository userRepository;


    // TODO Update Api : Verified And Assessment
    @RequestMapping(value = "/getAllAssessPatients", method = RequestMethod.GET)
    public ResponseEntity<?> getAllAssessPatients(String email) throws Exception {
        try {
            FindHelpUser currentDoctor = userRepository.findUserByUsername(email);
            if (null != currentDoctor) {
                List<String> patientListForDoctor = currentDoctor.getPatientQueue();
                return ResponseEntity.ok(userRepository.findFindHelpUserByEmail(patientListForDoctor));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Email Not Found"));
            }
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error."));
        }
    }


}
