package com.conu.findhelp.controller;

import com.conu.findhelp.dto.UserResponse;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.Manager;
import com.conu.findhelp.repositories.ManagerRepository;
import com.conu.findhelp.security.JwtTokenUtil;
import com.conu.findhelp.security.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    ManagerRepository managerRepository;

    @GetMapping("/testApi")
    public ResponseEntity<List<Manager>> testApi() {
        return new ResponseEntity<>(managerRepository.findAll(),null,HttpStatus.CREATED);
    }

//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse> loginManager(@RequestBody Manager employee) {
//        try {
//            Manager manager = managerRepository.findManagerByUsername(employee.getUsername(), employee.getPassword());
//            if (null == manager) {
//                return new ResponseEntity<>(new ApiResponse(400, "User Not Found",null), null, HttpStatus.BAD_REQUEST);
//            } else {
//                return new ResponseEntity<>(new ApiResponse(200, null,manager), null, HttpStatus.CREATED);
//            }
//        } catch (Exception ex) {
//            return new ResponseEntity<>(new ApiResponse(500, "Internal Server Error",null), null, HttpStatus.CREATED);
//        }
//    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Manager authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream().map(role->role.getAuthority()).collect(Collectors.toList());
        UserResponse user = new UserResponse(userDetails.getUsername(),token,roles);
        return ResponseEntity.ok(user);
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseEntity<?> signup(@RequestBody Manager signupRequest) throws Exception {
        try {
            final UserDetails userDetails = userDetailsService
                    .signupUser(signupRequest.getUsername(),signupRequest.getPassword());
            final String token = jwtTokenUtil.generateToken(userDetails);
            List<String> roles = userDetails.getAuthorities().stream().map(role->role.getAuthority()).collect(Collectors.toList());
            UserResponse user = new UserResponse(userDetails.getUsername(),token,roles);
            return ResponseEntity.ok(user);
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body("Error Processing the Request");
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }


//    @PostMapping("/signup")
//    public ResponseEntity<ApiResponse> signupManager(@RequestBody Manager employee) {
//        Manager manager = managerRepository.findManagerByUsername(employee.getUsername());
//        if(null == manager) {
//            return
//        }
//    }

}

