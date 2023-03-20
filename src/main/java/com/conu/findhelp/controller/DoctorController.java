package com.conu.findhelp.controller;

import com.conu.findhelp.dto.AddPatientCommentRequest;
import com.conu.findhelp.dto.UpdatePatientRequest;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RequestMapping("/api/v1/doctor")
@RestController
public class DoctorController {


    @Autowired
    UserRepository userRepository;

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

    @RequestMapping(value = "/updatePatientStatus", method = RequestMethod.POST)
    public ResponseEntity<?> getAllUsers(@RequestBody UpdatePatientRequest updatePatientRequest) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(updatePatientRequest.getEmail());
            if (null != currentUser) {
                if(currentUser.isDoctoringDone()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Patient doctoring already done."));
                }
                if(updatePatientRequest.getStatus().equals("REJECT_PATIENT")) {
                    currentUser.setCounsellorAssigned(null);
                    currentUser.setCounsellingDone(false);
                    currentUser.setAssessmentTaken(false);
                    currentUser.setAssessmentOptionsSelected(null);
                    currentUser.setDoctoringDone(false);
                    currentUser.setDoctorComment(null);
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Rejected Successfully."));
                }
                return ResponseEntity.status(400).body(new ApiResponse(400, false, "Invalid Operation Detected."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to updated Doesn't Exist"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }


    @RequestMapping(value = "/addPatientComment", method = RequestMethod.POST)
    public ResponseEntity<?> addComment(@RequestBody AddPatientCommentRequest addPatientCommentRequest ) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(addPatientCommentRequest.getEmail());
            if (null != currentUser) {
                if(currentUser.isCounsellingDone()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Patient Doctoring Already Done."));
                }
                currentUser.setCounsellingComment(currentUser.getCounsellingComment() != null  ?  currentUser.getCounsellingComment() + "," + addPatientCommentRequest.getComment() : addPatientCommentRequest.getComment()) ;
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient doctoring self assessment comment added successfully."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }

    @RequestMapping(value = "/markDoctoringDone", method = RequestMethod.POST)
    public ResponseEntity<?> markDoctoringDone(@RequestParam String patientEmail) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(patientEmail);
            if (null != currentUser) {
                currentUser.setCounsellingDone(true);
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Doctoring Done Successfully."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist."));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }


}
