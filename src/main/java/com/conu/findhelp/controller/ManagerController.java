package com.conu.findhelp.controller;


import com.conu.findhelp.dto.UserResponse;
import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.helpers.EmailService;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/manager")
@PreAuthorize("hasRole('ROLE_MANAGER')")
public class ManagerController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @RequestMapping(value = "/pendingUsers", method = RequestMethod.GET)
    public ResponseEntity<?> getPendingUsers(@RequestParam String role) throws Exception {
        List<FindHelpUser>  pendingUsers = userRepository.findFindHelpUserByStatusAndRole(STATUS.PENDING,role);
        pendingUsers = processUsers(pendingUsers);
        return ResponseEntity.ok(pendingUsers);
    }

    private List<FindHelpUser> processUsers(List<FindHelpUser> pendingUsers) {
        List<FindHelpUser> finalList = new ArrayList<>();
        for(FindHelpUser currentUser:pendingUsers) {
            currentUser.setPassword(null);
            finalList.add(currentUser);
        }
        return  finalList;
    }

    @RequestMapping(value = "/getAllUsers", method = RequestMethod.GET)
    public ResponseEntity<?> getAllUsers(@RequestParam String role) throws Exception {
        List<FindHelpUser>  allUsers = userRepository.findFindHelpUserByRole(role);
        allUsers = processUsers(allUsers);
        return ResponseEntity.ok(allUsers);
    }




    @RequestMapping(value = "/updateUser", method = RequestMethod.PUT)
    public ResponseEntity<?> updateUser(@RequestParam String userId,@RequestParam String status) throws Exception {
        try {
            FindHelpUser user = userRepository.findUserById(userId);
            if (null != user) {
                if (STATUS.DECLINED == STATUS.valueOf(status)) {
                    if (user.getRole().equals("ROLE_PATIENT")) {
                        user.setStatus(STATUS.valueOf(status));
                        userRepository.save(user);
                        emailService.sendSimpleMail(user.getEmail(),"Status Update","You have been declined by the manager. Kindly signup again using other id. Regards, We Care.");
                        return ResponseEntity.ok(new ApiResponse(200, false, "User Rejected from the System."));
                    } else if (user.getRole().equals("ROLE_DOCTOR") || user.getRole().equals("ROLE_COUNSELLOR")) {
                        userRepository.delete(user);
                        emailService.sendSimpleMail(user.getEmail(),"Status Update","You have been rejected by the manager. Kindly contact the manger for the approval. Regards, We Care.");
                        return ResponseEntity.ok(new ApiResponse(200, false, "User Rejected from the System."));
                    }
                } else if (STATUS.VERIFIED == STATUS.valueOf(status)) {
                    user.setStatus(STATUS.valueOf(status));
                    userRepository.save(user);
                    emailService.sendSimpleMail(user.getEmail(),"Status Update","Congratulations !!! You have been accepted by the manager. Regards, We Care.");
                    return ResponseEntity.ok(new ApiResponse(200, false, "User Verified Successfully"));
                }
            }
            return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Id Not Found"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Cannot Update User Status" + ex.getMessage()));
        }
    }



//    @RequestMapping(value = "/getCount", method = RequestMethod.POST)
//    public ResponseEntity<?> getCount(@RequestParam String startDate,@RequestParam String endDate) throws Exception {
//        return userRepository.getPatientsBetweenDate(startDate,endDate).size();
//    }

    @RequestMapping(value = "/exportUsers", method = RequestMethod.POST)
    public ResponseEntity<?> exportUsers(@RequestParam String startDate,@RequestParam String endDate) throws Exception {

        Date startD = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
        Date endD = new SimpleDateFormat("dd/MM/yyyy").parse(endDate);


        List<FindHelpUser>  patients = userRepository.getPatientsBetweenDate(startD,endD,"ROLE_PATIENT");

        ByteArrayInputStream byteArrayOutputStream;


        String[] csvHeader = {
                "id", "email", "role","name","address","dob","phone number","status","assessment taken","counselling done","doctor counselling done","counsellor assigned","doctor assigned","counselling result","doctor counselling result","creation date"
        };

        // closing resources by using a try with resources
        // https://www.baeldung.com/java-try-with-resources
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // defining the CSV printer
                CSVPrinter csvPrinter = new CSVPrinter(
                        new PrintWriter(out),
                        // withHeader is optional
                        CSVFormat.DEFAULT.withHeader(csvHeader)
                );
        ) {
            // populating the CSV content
            for (FindHelpUser patient : patients)
            {
                String [] values= patient.toString().split(",");
                csvPrinter.printRecord(values);
            }


            // writing the underlying stream
            csvPrinter.flush();

            byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        InputStreamResource fileInputStream = new InputStreamResource(byteArrayOutputStream);

        String csvFileName = "patients.csv";

        // setting HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
        // defining the custom Content-Type
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");

        return new ResponseEntity<>(
                fileInputStream,
                headers,
                HttpStatus.OK
        );
    }

}
