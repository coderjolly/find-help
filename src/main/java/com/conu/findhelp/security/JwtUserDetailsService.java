package com.conu.findhelp.security;

import com.conu.findhelp.models.Manager;
import com.conu.findhelp.repositories.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    ManagerRepository managerRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Manager manager = managerRepository.findManagerByUsername(username);
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (null != manager ) {
            return new User(manager.getUsername(), manager.getPassword(),
                    roles);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public UserDetails signupUser(String username,String password) throws Exception {
        Manager manager = managerRepository.findManagerByUsername(username);
        if(null == manager) {
            manager = new Manager(username,passwordEncoder.encode(password));
            managerRepository.insert(manager);
            return new User(manager.getUsername(), manager.getPassword(),
                    new ArrayList<>());
        } else {
            throw new Exception("User already exists");
        }

    }
}
