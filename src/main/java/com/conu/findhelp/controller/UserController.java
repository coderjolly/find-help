package com.conu.findhelp.controller;

import com.conu.findhelp.dto.UserResponse;
import com.conu.findhelp.enums.STATUS;
import com.conu.findhelp.exceptions.UserAlreadyExistsException;
import com.conu.findhelp.helpers.EmailService;
import com.conu.findhelp.models.ApiResponse;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import com.conu.findhelp.security.JwtTokenUtil;
import com.conu.findhelp.security.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

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

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public ResponseEntity<?> forgotPassword(@RequestParam String email) throws Exception {
        try {
            FindHelpUser currentUser = userDetailsService.getUser(email);
            if(null != currentUser) {
                String OTP = generateOTP();
                boolean status = emailService.sendSimpleMail(currentUser.getEmail(),"Forgot Password","You otp to reset password is "+OTP);
                if(!status) {
                    return ResponseEntity.status(500).body(new ApiResponse(500,true,"Error is sending email to User. Kindly check your email."));
                }
                currentUser.setOTP(OTP);
                currentUser.setVerificationAttempts(5);
                Calendar date = Calendar.getInstance();
                long timeInSecs = date.getTimeInMillis();
                Date expiryDate = new Date(timeInSecs + (2 * 60 * 1000));
                currentUser.setOtpExpiryDate(expiryDate);
                userRepository.save(currentUser);
                return ResponseEntity.status(200).body(new ApiResponse(200,false,"OTP Sent Successfully."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400,true,"User Doesn't Exist."));
            }
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500,true,"Error in sending email. "+ex.getMessage()));
        }
    }

    @RequestMapping(value = "/verifyOTP", method = RequestMethod.POST)
    public ResponseEntity<?> verifyOTP(@RequestParam String email,@RequestParam String otp) throws Exception {
        try {
            FindHelpUser currentUser = userDetailsService.getUser(email);
            if(null != currentUser) {
                Date currentDate = new Date();
                if(currentDate.compareTo(currentUser.getOtpExpiryDate())> 0) {
                    return ResponseEntity.status(400).body(new ApiResponse(400,true,"OTP expired."));
                } else if(currentUser.getVerificationAttempts()==0) {
                    return ResponseEntity.status(400).body(new ApiResponse(400,true,"Verification Attempts Finished"));
                }
                if(otp.equals(currentUser.getOTP())) {
                    return ResponseEntity.status(200).body(new ApiResponse(200,false,"OTP Verified Successfully."));
                } else {
                    userRepository.save(currentUser);
                    currentUser.setVerificationAttempts(currentUser.getVerificationAttempts()-1);
                }
                return ResponseEntity.status(400).body(new ApiResponse(400,true,"OTP Doesn't Match. Kindly Retry with valid OTP."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400,true,"Couldn't Verify OTP. Uses doesn't exist."));
            }
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500,true,"Error in verifying OTP." +ex.getMessage()));
        }
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@RequestParam String email,@RequestParam String otp,@RequestParam String newPassword) throws Exception {
        try {
            FindHelpUser currentUser = userDetailsService.getUser(email);
            if(null != currentUser) {
                Date currentDate = new Date();
                if(currentDate.compareTo(currentUser.getOtpExpiryDate())> 0) {
                    return ResponseEntity.status(400).body(new ApiResponse(400,true,"OTP expired."));
                } else if(currentUser.getVerificationAttempts()==0) {
                    return ResponseEntity.status(400).body(new ApiResponse(400,true,"Verification Attempts Finished"));
                }
                if(otp.equals(currentUser.getOTP())) {
                    currentUser.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(currentUser);
                    return ResponseEntity.status(200).body(new ApiResponse(200,false,"Password Reset Successful. Kindly Login."));
                } else {
                    currentUser.setVerificationAttempts(currentUser.getVerificationAttempts()-1);
                    userRepository.save(currentUser);
                }
                return ResponseEntity.status(400).body(new ApiResponse(400,true,"OTP Doesn't Match. Kindly Retry with valid OTP."));
            } else {
                return ResponseEntity.status(400).body(new ApiResponse(400,true," User doesn't exist with this email Id."));
            }
        }
        catch (Exception ex) {
            return ResponseEntity.status(500).body(new ApiResponse(500,true,"Error in verifying OTP." +ex.getMessage()));
        }
    }

    private String generateOTP() {
        String numbers = "0123456789";
        // Using random method
        Random rndm_method = new Random();

        char[] password = new char[6];

        for (int i = 0; i < 6; i++)
        {
            // Use of charAt() method : to get character value
            // Use of nextInt() as it is scanning the value as int
            password[i] =
                    numbers.charAt(rndm_method.nextInt(numbers.length()));

        }

        return String.valueOf(password);
    }


}

