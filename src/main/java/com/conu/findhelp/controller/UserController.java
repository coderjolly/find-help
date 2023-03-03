package com.conu.findhelp.controller;

import com.conu.findhelp.dto.UserResponse;
import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.exceptions.UserAlreadyExistsException;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import com.conu.findhelp.security.JwtTokenUtil;
import com.conu.findhelp.security.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestBody FindHelpUser loginRequest) throws Exception {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            FindHelpUser currentUser = userDetailsService.getUser(loginRequest.getEmail());
            if(currentUser.getStatus()== STATUS.PENDING) {
                return ResponseEntity.status(400).body(new ApiResponse(400,true,"You are not verified yet. Kindly Contact the Manager"));
            } else if(currentUser.getStatus()== STATUS.DECLINED) {
                return ResponseEntity.status(400).body(new ApiResponse(400,true,"You are declined from the system. Kindly Contact the Manager"));
            }
            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(loginRequest.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails,loginRequest.getRole());
            currentUser.setPassword(null);
            UserResponse user = new UserResponse(currentUser,token);
            return ResponseEntity.ok(user);
        }
         catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new ApiResponse(401,true,"Credentials does not match"));
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500,true,"Cannot Login Due to" + ex.getMessage()));
        }
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<?> signup(@RequestBody FindHelpUser signupUserRequest) throws Exception {
        try {
            final UserDetails userDetails = userDetailsService
                    .signupUser(signupUserRequest);
            if(signupUserRequest.getRole().equals("ROLE_PATIENT")) {
                return ResponseEntity.ok(new ApiResponse(200,false,"User SignUp SuccessFull."));
            } else {
                return ResponseEntity.ok(new ApiResponse(200,false,"User SignUp SuccessFull. You account will be verified soon."));
            }
        }
        catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(400).body(new ApiResponse(400,true,"Couldn't Signup User. User Already Exists"));
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500,true,"Couldn't Signup User. "+ex.getMessage()));
        }
    }


}

