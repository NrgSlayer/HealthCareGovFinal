package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user=userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        SimpleGrantedAuthority authority=new SimpleGrantedAuthority("ROLE_"+user.getRole().toUpperCase());
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(), List.of(authority));

    }
}
