package com.conu.findhelp.security;

import com.conu.findhelp.exceptions.UserAlreadyExistsException;
import com.conu.findhelp.models.FindHelpUser;
import com.conu.findhelp.repositories.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        FindHelpUser user = userRepository.findUserByUsername(username);
        if (null != user ) {
            List<GrantedAuthority> roles = new ArrayList<>();
            roles.add(new SimpleGrantedAuthority(user.getRole()));
            return new User(user.getEmail(), user.getPassword(),
                    roles);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public FindHelpUser getUser(String userId) {
        return userRepository.findUserByUsername(userId);
    }

    public UserDetails signupUser(FindHelpUser signupUserRequest) throws Exception {

        FindHelpUser user  = userRepository.findUserByUsername(signupUserRequest.getEmail());
        if(null == user) {
            if(signupUserRequest.getRole().equals("ROLE_PATIENT")){
                user = new FindHelpUser(signupUserRequest.getEmail(),passwordEncoder.encode(signupUserRequest.getPassword()),signupUserRequest.getName(),signupUserRequest.getDob(),signupUserRequest.getPhone(),signupUserRequest.getAddress(),signupUserRequest.getRole());
            } else if(signupUserRequest.getRole().equals("ROLE_DOCTOR") || signupUserRequest.getRole().equals("ROLE_COUNSELLOR") ) {
                user = new FindHelpUser(signupUserRequest.getEmail(),passwordEncoder.encode(signupUserRequest.getPassword()),signupUserRequest.getName(),signupUserRequest.getDob(),signupUserRequest.getPhone(),signupUserRequest.getAddress(),signupUserRequest.getRegistrationNo(),signupUserRequest.getRole());
            } else  {
                throw new Exception("Undetermined Role Coming Up");
            }
            userRepository.insert(user);
            List<GrantedAuthority> roles = new ArrayList<>();
            roles.add(new SimpleGrantedAuthority(user.getRole()));
            return new User(user.getEmail(), user.getPassword(),
                    roles);
        } else {
            throw new UserAlreadyExistsException("User already exists");
        }
    }

//    public UserDetails signupUser(String username,String password) throws Exception {
//        Manager manager = managerRepository.findManagerByUsername(username);
//        if(null == manager) {
//            manager = new Manager(username,passwordEncoder.encode(password));
//            managerRepository.insert(manager);
//            return new User(manager.getUsername(), manager.getPassword(),
//                    new ArrayList<>());
//        } else {
//            throw new Exception("User already exists");
//        }
//
//    }
}
