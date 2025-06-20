package com.di.marinetracker.backendspringboot.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.di.marinetracker.backendspringboot.dto.JWTResponseDTO;
import com.di.marinetracker.backendspringboot.dto.LoginRequestDTO;
import com.di.marinetracker.backendspringboot.dto.MessageResponseDTO;
import com.di.marinetracker.backendspringboot.dto.SignupRequestDTO;
import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.services.UserDetailsImpl;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Allows cross-origin requests from any origin with a max age of 3600 seconds
@CrossOrigin(origins = "*", maxAge = 3600)
// Marks this class as a REST controller and sets the base request mapping
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // Injects the AuthenticationManager for handling authentication
    @Autowired
    AuthenticationManager authenticationManager;

    // Injects the UserRepository for user data access
    @Autowired
    UserRepository userRepository;

    // Injects the PasswordEncoder for encoding user passwords
    @Autowired
    PasswordEncoder encoder;

    // Injects the JwtUtils for JWT operations
    @Autowired
    JwtUtils jwtUtils;

    // Handles user login POST requests
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        // Creates an authentication token using the provided username and password
        UsernamePasswordAuthenticationToken token =  new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        // Authenticates the user
        Authentication authentication = authenticationManager.authenticate(token);

        // Sets the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Generates a JWT token for the authenticated user
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Retrieves user details and roles
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        // Creates a secure cookie with the JWT token
        String cookieValue = String.format(
            "jwt=%s; Path=/; Max-Age=%d; SameSite=None; Secure; HttpOnly",
            jwt, 24 * 60 * 60
        );

        // Sets the cookie in the response header
        response.setHeader("Set-Cookie", cookieValue);

        // Returns the JWT and user details in the response body
        return ResponseEntity.ok(new JWTResponseDTO(jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles));
    }

    // Handles user logout POST requests
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Creates a cookie with max age 0 to remove the JWT cookie from the client
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        // Adds the expired cookie to the response
        response.addCookie(cookie);

        // Returns an empty OK response
        return ResponseEntity.ok().build();
    }

    // Handles user registration POST requests
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDTO signUpRequest) {
        // Checks if the email is already in use
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponseDTO("Error: Email is already in use!"));
        }
        // Checks if the username is already in use
        if (userRepository.existsByUserName(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDTO("Error: Username is already in use!"));
        }

        // Retrieves roles from the signup request or defaults to "user"
        Set<String> strRoles = signUpRequest.getRole();
        Set<String> roles = new HashSet<>();
        if (strRoles == null) {
            strRoles = Set.of("user");
        }

        // Adds each role to the roles set
        strRoles.forEach(role -> {
            roles.add(role);
        });

        // Creates a new user entity with the provided details and encoded password
        User user = new User(
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            encoder.encode(signUpRequest.getPassword()),
            roles);

        // Saves the new user to the repository
        userRepository.save(user);
        // Returns a success message
        return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!"));
    }
}
