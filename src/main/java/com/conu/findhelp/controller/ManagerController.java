package com.conu.findhelp.controller;


import com.conu.findhelp.dto.UserResponse;
import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.helpers.EmailService;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
                        emailService.sendSimpleMail(user.getEmail(),"Status Update","You have been declined by the manager. Kindly signup again using other id.");
                        return ResponseEntity.ok(new ApiResponse(200, false, "User Rejected from the System."));
                    } else if (user.getRole().equals("ROLE_DOCTOR") || user.getRole().equals("ROLE_COUNSELLOR")) {
                        userRepository.delete(user);
                        emailService.sendSimpleMail(user.getEmail(),"Status Update","You have been rejected by the manager. Kindly contact the manger for the approval.");
                        return ResponseEntity.ok(new ApiResponse(200, false, "User Rejected from the System."));
                    }
                } else if (STATUS.VERIFIED == STATUS.valueOf(status)) {
                    user.setStatus(STATUS.valueOf(status));
                    userRepository.save(user);
                    emailService.sendSimpleMail(user.getEmail(),"Status Update","Congratulations !!! You have accepted by the manager.");
                    return ResponseEntity.ok(new ApiResponse(200, false, "User Verified Successfully"));
                }
            }
            return ResponseEntity.status(400).body(new ApiResponse(400, true, "User with Id Not Found"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500, true, "Cannot Update User Status" + ex.getMessage()));
        }
    }

}
