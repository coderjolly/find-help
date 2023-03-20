package com.conu.findhelp.controller;

import com.conu.findhelp.dto.AddPatientCommentRequest;
import com.conu.findhelp.dto.UpdatePatientRequest;
import com.conu.findhelp.enums.STATUS;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/api/v1/counsellor")
public class CounsellorController {



    @Autowired
    UserRepository userRepository;


    // TODO Update Api : Verified And Assessment
    @RequestMapping(value = "/getAllAssessPatients", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUsers(@RequestParam String email) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(email);
            if (null != currentUser) {
                List<String> patientList = currentUser.getPatientQueue();
                return ResponseEntity.ok(userRepository.findFindHelpUserByEmail(patientList));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Email Not Found"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }

    }



    @RequestMapping(value = "/updatePatientStatus", method = RequestMethod.POST)
    public ResponseEntity<?> getAllUsers(@RequestBody UpdatePatientRequest updatePatientRequest) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(updatePatientRequest.getEmail());
            if (null != currentUser) {
                if(currentUser.isCounsellingDone()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Patient counselling already done."));
                }
                if(updatePatientRequest.getStatus().equals("ASSIGN_DOCTOR")){
                    assignDoctor(currentUser);
                    currentUser.setCounsellingComment(updatePatientRequest.getReason());
                    currentUser.setCounsellingDone(true);
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Doctor Assigned Successfully."));
                }
                else if(updatePatientRequest.getStatus().equals("REJECT_PATIENT")) {
                    currentUser.setCounsellingComment(updatePatientRequest.getReason());
                    currentUser.setCounsellingDone(true);
                    currentUser.setAssessmentTaken(false);
                    currentUser.setAssessmentOptionsSelected(null);
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Rejected Successfully."));
                }
                return ResponseEntity.status(200).body(new ApiResponse(400, false, "Invalid Operation on Patient."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }

    private void assignDoctor(FindHelpUser currentUser) {
        List<FindHelpUser> doctors = userRepository.findFindHelpUserByStatusAndRole(STATUS.VERIFIED, "ROLE_DOCTOR");
        Collections.sort(doctors, (o1, o2) -> {
            if(o1.getPatientQueue() == null  ) return -1;
            if(o2.getPatientQueue() == null  ) return -1;
           return o1.getPatientQueue().size() - o2.getPatientQueue().size();
        });
        FindHelpUser doctorWithLeastPatient = doctors.get(0);
        List<String> doctorQueue = doctorWithLeastPatient.getPatientQueue() == null ? new ArrayList<>() : doctorWithLeastPatient.getPatientQueue();
        doctorQueue.add(currentUser.getEmail());
        doctorWithLeastPatient.setPatientQueue(doctorQueue);
        userRepository.save(doctorWithLeastPatient);
        currentUser.setDoctorAssigned(doctorWithLeastPatient.getEmail());
        userRepository.save(currentUser);
    }


    @RequestMapping(value = "/addPatientComment", method = RequestMethod.POST)
    public ResponseEntity<?> addComment(@RequestBody AddPatientCommentRequest addPatientCommentRequest ) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(addPatientCommentRequest.getEmail());
            if (null != currentUser) {
                if(currentUser.isCounsellingDone()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Patient Counselling Already Done."));
                }
                currentUser.setCounsellingComment(currentUser.getCounsellingComment() != null  ?  currentUser.getCounsellingComment() + "," + addPatientCommentRequest.getComment() : addPatientCommentRequest.getComment()) ;
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient counselling self assessment comment added successfully."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }

    @RequestMapping(value = "/markCounsellingDone", method = RequestMethod.POST)
    public ResponseEntity<?> markCounsellingDone(@RequestParam String patientEmail) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(patientEmail);
            if (null != currentUser) {
                currentUser.setCounsellingDone(true);
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Counselling Done Successfully"));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }



}
