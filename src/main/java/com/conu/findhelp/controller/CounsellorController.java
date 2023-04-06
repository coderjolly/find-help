package com.conu.findhelp.controller;

import com.conu.findhelp.dto.AddPatientCommentRequest;
import com.conu.findhelp.dto.UpdatePatientRequest;
import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.helpers.EmailService;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.dto.Slot;
import com.conu.findhelp.repositories.UserRepository;
import com.conu.findhelp.security.JwtTokenUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@PreAuthorize("hasRole('ROLE_COUNSELLOR')")
@RequestMapping("/api/v1/counsellor")
public class CounsellorController {



    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Autowired
    EmailService emailService;


    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/getAllAssessPatients", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUsers(@RequestParam String email) throws Exception {
        try {
            FindHelpUser currentUser = userRepository.findUserByUsername(email);
            if (null != currentUser) {
                List<String> patientList = currentUser.getPatientQueue() !=null ? currentUser.getPatientQueue() : new ArrayList<>();
                return ResponseEntity.ok(userRepository.findFindHelpUserByEmail(patientList));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Email Not Found"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }

    }

    @RequestMapping(value = "/getAllDoctors", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUsers() throws Exception {
        List<FindHelpUser>  allDoctors = userRepository.findFindHelpUserByStatusAndRole(STATUS.VERIFIED,"ROLE_DOCTOR");
        allDoctors = processUsers(allDoctors);
        return ResponseEntity.ok(allDoctors);
    }

    private List<FindHelpUser> processUsers(List<FindHelpUser> pendingUsers) {
        List<FindHelpUser> finalList = new ArrayList<>();
        for(FindHelpUser currentUser:pendingUsers) {
            currentUser.setPassword(null);
            finalList.add(currentUser);
        }
        return  finalList;
    }



    @RequestMapping(value = "/updatePatientStatus", method = RequestMethod.POST)
    public ResponseEntity<?> getAllUsers(@RequestBody UpdatePatientRequest updatePatientRequest) throws Exception {
        try {
            FindHelpUser patient = userRepository.findUserByUsername(updatePatientRequest.getPatientEmail());
            if (null != patient) {
                if(patient.isCounsellingDone()) {
                    return ResponseEntity.status(400).body(new ApiResponse(400, true, "Patient counselling already done."));
                }
                if(updatePatientRequest.getStatus().equals("ASSIGN_DOCTOR")){
                    assignDoctor(patient);
                    patient.setCounsellingComment(updatePatientRequest.getReason());
                    patient.setCounsellingDone(true);
                    userRepository.save(patient);
                    FindHelpUser counsellor = userRepository.findUserByUsername(updatePatientRequest.getCounsellorEmail());
                    List<String> patientQueue  = counsellor.getPatientQueue();
                    patientQueue.remove(updatePatientRequest.getPatientEmail());
                    counsellor.setPatientQueue(patientQueue);
                    userRepository.save(counsellor);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Doctor Assigned Successfully."));
                }
                else if(updatePatientRequest.getStatus().equals("REJECT_PATIENT")) {
                    patient.setCounsellingComment(updatePatientRequest.getReason());
                    patient.setCounsellingDone(false);
                    patient.setAssessmentTaken(false);
                    patient.setAssessmentOptionsSelected(null);
                    userRepository.save(patient);
                    FindHelpUser counsellor = userRepository.findUserByUsername(updatePatientRequest.getCounsellorEmail());
                    List<String> patientQueue  = counsellor.getPatientQueue();
                    patientQueue.remove(updatePatientRequest.getPatientEmail());
                    counsellor.setPatientQueue(patientQueue);
                    userRepository.save(counsellor);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Rejected Successfully."));
                }
                else if(updatePatientRequest.getStatus().equals("SELF_ASSIGN")) {
                    FindHelpUser counsellor = userRepository.findUserByUsername(updatePatientRequest.getCounsellorEmail());
                    List<String> patientQueue  = counsellor.getPatientQueue();
                    patientQueue.remove(updatePatientRequest.getPatientEmail());
                    counsellor.setPatientQueue(patientQueue);
                    userRepository.save(counsellor);
                    patient.setCounsellingComment(patient.getCounsellingComment() != null  ?  patient.getCounsellingComment() + "," + updatePatientRequest.getReason() : updatePatientRequest.getReason()) ;
                    userRepository.save(patient);
                    return ResponseEntity.status(200).body(new ApiResponse(200, false, "Patient Comment Added Successfully."));
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
    public ResponseEntity<?> assignSlot(@RequestParam String counsellorEmail, @RequestParam String patientEmail, @RequestParam String date , @RequestParam String slotTime ) throws Exception {
        try {
            FindHelpUser counsellor = userRepository.findUserByUsername(counsellorEmail);
            if (null != counsellor) {

                FindHelpUser patient = userRepository.findUserByUsername(patientEmail);
                HashMap<String,List<Slot>> appointments =    counsellor.getAppointments();
                List<Slot> slots = appointments.get(date);

                List<Slot> finalSlots = new ArrayList<>();
                for(Slot currSlot:slots) {
                    if(currSlot.getSlotTime().equals(slotTime)) {
                        currSlot.setSlotAssignedTo(patientEmail);
                        currSlot.setName(patient.getName());
                        currSlot.setStatus("ASSIGNED");
                    }
                    finalSlots.add(currSlot);
                }
                appointments.put(date,finalSlots);
                counsellor.setAppointments(appointments);
                userRepository.save(counsellor);

                HashMap<String,List<Slot>> patientAppointments = patient.getAppointments();
                if(patientAppointments.containsKey(date)) {
                    List<Slot> patientsSlotForDate = patientAppointments.get(date);
                    Optional<Slot> slotPresent = patientsSlotForDate.stream().filter(slot -> slot.getSlotTime().equals(slotTime) && Objects.nonNull(slot.getStatus())).findFirst();
                    if(slotPresent.isPresent())  {
                        return ResponseEntity.status(400).body(new ApiResponse(400, true, "Slot you are trying to update in already used."));
                    }
                    Slot patientSlot = new Slot(slotTime,counsellor.getName(),"ASSIGNED",counsellorEmail,null,"COUNSELLOR");
                    patientsSlotForDate.add(patientSlot);
                    patientAppointments.put(date,patientsSlotForDate);
                } else {
                    List<Slot> patientsSlotForDate = new ArrayList<>();
                    Slot patientSlot = new Slot(slotTime,counsellor.getName(),"ASSIGNED",counsellorEmail,null,"COUNSELLOR");
                    patientsSlotForDate.add(patientSlot);
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

    @RequestMapping(value = "/updateAppointmentSlot", method = RequestMethod.POST)
    public ResponseEntity<?> updateAppointmentSlot(@RequestParam String counsellorEmail, @RequestParam String patientEmail, @RequestParam String existingDate , @RequestParam String existingSlotTime , @RequestParam String newDate, @RequestParam String newSlotTime ) throws Exception {
        try {
            FindHelpUser counsellor = userRepository.findUserByUsername(counsellorEmail);
            FindHelpUser patient = userRepository.findUserByUsername(patientEmail);

            if (null != counsellor && null!= patient) {
                HashMap<String,List<Slot>> counsellorAppointments = counsellor.getAppointments();
                if(counsellorAppointments.containsKey(existingDate)) {
                    List<Slot> slots = counsellorAppointments.get(existingDate);
                    List<Slot> newSlots = new ArrayList<>();
                    for(Slot slot:slots) {
                        if(Objects.nonNull(slot.getSlotAssignedTo()) && slot.getSlotAssignedTo().equals(patientEmail) && slot.getSlotTime().equals(existingSlotTime)) {
                            slot.setStatus("DISCARDED");
                        }
                        newSlots.add(slot);
                    }
                    counsellorAppointments.put(existingDate,newSlots);
                    counsellor.setAppointments(counsellorAppointments);
                    userRepository.save(counsellor);
                    HashMap<String,List<Slot>> patientAppointments = patient.getAppointments();
                    List<Slot> patientsSlots = patientAppointments.get(existingDate);
                    List<Slot> patientNewSlots = new ArrayList<>();
                    for(Slot slot:patientsSlots) {
                        if (Objects.nonNull(slot.getSlotAssignedBy()) && slot.getSlotAssignedBy().equals(counsellorEmail) && slot.getSlotTime().equals(existingSlotTime)) {
                            slot.setStatus("DISCARDED");
                        }
                        patientNewSlots.add(slot);
                    }

                    patientAppointments.put(existingDate,patientNewSlots);
                    patient.setAppointments(patientAppointments);
                    userRepository.save(patient);
                    assignSlot(counsellorEmail,patientEmail,newDate,newSlotTime);
                    return ResponseEntity.status(200).body(new ApiResponse(200, true, "Updated Slots"));
                } else {
                    return ResponseEntity.status(400).body(new ApiResponse(200, true, "Current Slot Not Found"));
                }
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400, true, "Invalid Request to Update Slot"));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Internal Server Error"));
        }
    }




}
