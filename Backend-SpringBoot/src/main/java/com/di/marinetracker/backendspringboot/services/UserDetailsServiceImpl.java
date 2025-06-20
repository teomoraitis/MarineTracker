package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Implementation of UserDetailsService for Spring Security
// Needed to load user-specific data during authentication
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Injects the UserRepository for user data access
    @Autowired
    UserRepository userRepository;

    // Loads user details by username
    @Override
    @Transactional
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Builds and returns UserDetailsImpl from the User entity
        return UserDetailsImpl.build(user);
    }

}