package com.conu.findhelp.controller;


import com.conu.findhelp.dto.AssessmentDetails;
import com.conu.findhelp.dto.Slot;
import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.helpers.EmailService;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.dto.UpdateSlotRequest;
import com.conu.findhelp.repositories.UserRepository;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/patient")
@PreAuthorize("hasRole('ROLE_PATIENT')")
public class PatientController {


    @Autowired
    EmailService emailService;
    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/addAssessDetails", method = RequestMethod.POST)
    public ResponseEntity<?> addAssessDetails(@RequestBody AssessmentDetails assessmentDetails) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(assessmentDetails.getEmail());
            if (null != currentUser) {
                if(currentUser.isAssessmentTaken()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Assessment already taken."));
                }
                assignCounsellor(currentUser);
                currentUser.setAssessmentOptionsSelected(assessmentDetails.getAssessmentOptionsSelected());
                currentUser.setAssessmentTaken(true);
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Assessment taken successfully"));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Email Not Found"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }

    private void assignCounsellor(FindHelpUser currentUser) {
        List<FindHelpUser> counsellors = userRepository.findFindHelpUserByStatusAndRole(STATUS.VERIFIED, "ROLE_COUNSELLOR");
        Collections.sort(counsellors, new Comparator<FindHelpUser>() {
            @Override
            public int compare(FindHelpUser o1, FindHelpUser o2) {
                if(o1.getPatientQueue() == null  ) return -1;
                if(o2.getPatientQueue() == null  ) return -1;
                return o1.getPatientQueue().size() - o2.getPatientQueue().size();
            }
        });
        FindHelpUser counsellorWithLeastPatient = counsellors.get(0);
        List<String> patientQueue = counsellorWithLeastPatient.getPatientQueue() == null ? new ArrayList<>() : counsellorWithLeastPatient.getPatientQueue();
        patientQueue.add(currentUser.getEmail());
        counsellorWithLeastPatient.setPatientQueue(patientQueue);
        userRepository.save(counsellorWithLeastPatient);
        currentUser.setCounsellorAssigned(counsellorWithLeastPatient.getEmail());
        userRepository.save(currentUser);
    }


    @RequestMapping(value = "/removeAssessDetails", method = RequestMethod.POST)
    public ResponseEntity<?> removeAssessDetails(@RequestParam String email) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(email);
            if (null != currentUser) {
                if(!currentUser.isAssessmentTaken()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Assessment not taken."));
                }
                currentUser.setAssessmentOptionsSelected(null);
                String counsellorEmail = currentUser.getCounsellorAssigned();
                FindHelpUser counsellor =  userRepository.findUserByUsername(counsellorEmail);
                List<String> patientQueue = counsellor.getPatientQueue();
                patientQueue.remove(email);
                counsellor.setPatientQueue(patientQueue);
                userRepository.save(counsellor);
                currentUser.setAssessmentTaken(false);
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Assessment cancelled successfully"));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Email Not Found"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }


    @RequestMapping(value = "/updateAppointmentStatus", method = RequestMethod.POST)
    public ResponseEntity<?> updateSlot(@RequestBody UpdateSlotRequest updateSlotRequest) throws Exception {
        String patientEmail = ThreadContext.get("userId");
        try {
            FindHelpUser patient = userRepository.findUserByUsername(patientEmail);
            if (null != patient) {
                HashMap<String,List<Slot>> appointments = patient.getAppointments();
                List<Slot> currentSlots = appointments.get(updateSlotRequest.getDate());
                List<Slot> newSlots = new ArrayList<>();
                for(Slot slot:currentSlots) {
                    if(slot.getSlotAssignedBy().equals(updateSlotRequest.getAssignedBy())) {
                        if(updateSlotRequest.getStatus().equals("ACCEPTED")) {
               emailService.sendSimpleMail(slot.getSlotAssignedBy(),"Appointment Update","Patient : "+ patient.getName()+ " (" + patientEmail +") has accepted the appointment."  );
                        slot.setStatus("ACCEPTED");
                        } else if(updateSlotRequest.getStatus().equals("REJECTED")) {
          emailService.sendSimpleMail(slot.getSlotAssignedBy(),"Appointment Update","Patient : "+ patient.getName()+ " (" + patientEmail +") has rejected the appointment. Kindly schedule new appointment."  );
                            slot.setStatus("REJECTED");
                        }
                        FindHelpUser counsellorOrDoctor = userRepository.findUserByUsername(slot.getSlotAssignedBy());
                        List<Slot> counsellorOrDoctorSlots =  counsellorOrDoctor.getAppointments().get(updateSlotRequest.getDate());
                        List<Slot>  newSlotsForCounsellor = new ArrayList<>();
                        for(Slot currSlot:counsellorOrDoctorSlots) {
                            if(null!=currSlot.getSlotAssignedTo() && currSlot.getSlotAssignedTo().equals(patientEmail)) {
                                currSlot.setStatus(updateSlotRequest.getStatus());
                            }
                            newSlotsForCounsellor.add(currSlot);
                        }

                        HashMap<String,List<Slot>> counsellorOrDoctorAppointment = counsellorOrDoctor.getAppointments();
                        if(null!=counsellorOrDoctorAppointment) {
                            counsellorOrDoctorAppointment.put(updateSlotRequest.getDate(),newSlotsForCounsellor);
                            counsellorOrDoctor.setAppointments(counsellorOrDoctorAppointment);
                        }
                        userRepository.save(counsellorOrDoctor);
                    }
                    newSlots.add(slot);
                }
                appointments.put(updateSlotRequest.getDate(),newSlots);
                patient.setAppointments(appointments);
                userRepository.save(patient);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Appointment updated successfully"));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Email Not Found"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }




}
