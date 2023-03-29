package com.conu.findhelp.controller;

import com.conu.findhelp.dto.AddPatientCommentRequest;
import com.conu.findhelp.dto.UpdatePatientRequest;
import com.conu.findhelp.helpers.EmailService;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.dto.Slot;
import com.conu.findhelp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/doctor")
@RestController
public class DoctorController {


    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/getAllAssessPatients", method = RequestMethod.GET)
    public ResponseEntity<?> getAllAssessPatients(String email) throws Exception {
        try {
            FindHelpUser currentDoctor = userRepository.findUserByUsername(email);
            if (null != currentDoctor) {
                List<String> patientListForDoctor = currentDoctor.getPatientQueue() !=null ? currentDoctor.getPatientQueue() : new ArrayList<>();
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
            FindHelpUser currentUser = userRepository.findUserByUsername(updatePatientRequest.getDoctorEmail());
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
                    FindHelpUser doctor = userRepository.findUserByUsername(updatePatientRequest.getDoctorEmail());
                    List<String> patientQueue  = doctor.getPatientQueue();
                    patientQueue.remove(updatePatientRequest.getPatientEmail());
                    doctor.setPatientQueue(patientQueue);
                    userRepository.save(doctor);
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Rejected Successfully."));
                } else if(updatePatientRequest.getStatus().equals("SELF_ASSIGN")) {
                    currentUser.setDoctorComment(currentUser.getDoctorComment() != null  ?  currentUser.getDoctorComment() + "," + updatePatientRequest.getReason() : updatePatientRequest.getReason()); ;
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Doctor Comment Added Successfully."));
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


    @RequestMapping(value = "/getAppointmentSlots", method = RequestMethod.GET)
    public ResponseEntity<?> getSlots(@RequestParam String email, @RequestParam String date) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(email);
            if (null != currentUser) {
                HashMap<String,List<Slot>> appointments = currentUser.getAppointments();
                if(appointments.containsKey(date)) {
                    List<Slot> totalSlots = appointments.get(date);
                    List<String> slots = totalSlots.stream().filter(slot-> Objects.isNull(slot.getSlotAssignedTo())).map(e-> e.getSlotTime()).collect(Collectors.toList());
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, slots));
                } else {
                    String [] slotsTime = new String[]{"12:00-13:00","13:00-14:00","14:00-15:00","15:00-16:00","16:00-17:00","17:00-18:00"};
                    List<Slot> slots = new ArrayList<>();
                    for(String slottime:slotsTime) {
                        slots.add(new Slot(slottime,null,null,null,null,null));
                    }
                    appointments.put(date,slots);
                    List<String> finalSlots = slots.stream().filter(slot-> Objects.isNull(slot.getSlotAssignedTo())).map(e-> e.getSlotTime()).collect(Collectors.toList());
                    currentUser.setAppointments(appointments);
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, finalSlots));
                }
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist."));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }


    @RequestMapping(value = "/setAppointmentSlot", method = RequestMethod.POST)
    public ResponseEntity<?> assignSlot(@RequestParam String doctorEmail, @RequestParam String patientEmail, @RequestParam String date , @RequestParam String slotTime ) throws Exception {
        try {
            FindHelpUser doctor = userRepository.findUserByUsername(doctorEmail);
            if (null != doctor) {
                HashMap<String,List<Slot>> appointments =    doctor.getAppointments();
                List<Slot> slots = appointments.get(date);
                List<Slot> finalSlots = new ArrayList<>();
                for(Slot currSlot:slots) {
                    if(currSlot.getSlotTime().equals(slotTime)) {
                        currSlot.setSlotAssignedTo(patientEmail);
                        currSlot.setName(doctor.getName());
                        currSlot.setStatus("ASSIGNED");
                    }
                    finalSlots.add(currSlot);
                }
                appointments.put(date,finalSlots);
                doctor.setAppointments(appointments);
                userRepository.save(doctor);

                FindHelpUser patient = userRepository.findUserByUsername(patientEmail);
                HashMap<String,List<Slot>> patientAppointments = patient.getAppointments();
                if(patientAppointments.containsKey(date)) {
                    List<Slot> patientsSlotForDate = patientAppointments.get(date);
                    Slot patientSlot = new Slot(slotTime,doctor.getName(),"ASSIGNED",doctorEmail,null,"DOCTOR");
                    patientsSlotForDate.add(patientSlot);
                } else {
                    List<Slot> patientsSlotForDate = new ArrayList<>();
                    Slot patientSlot = new Slot(slotTime,doctor.getName(),"ASSIGNED",doctorEmail,null,"DOCTOR");
                    patientAppointments.put(date,patientsSlotForDate);
                }
                patient.setAppointments(patientAppointments);
                userRepository.save(patient);
                emailService.sendSimpleMail(patientEmail,"Appointment Scheduled","You appointment is scheduled for date: "+date + " "+ slotTime);
                return ResponseEntity.status(200).body(new ApiResponse(200, false, "Appointment Sent Successfully"));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User you are trying to update Doesn't Exist."));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }


}
