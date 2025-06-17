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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token =  new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        String cookieValue = String.format(
            "jwt=%s; Path=/; Max-Age=%d; SameSite=None; Secure; HttpOnly",
            jwt, 24 * 60 * 60
        );

        response.setHeader("Set-Cookie", cookieValue);

        return ResponseEntity.ok(new JWTResponseDTO(jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDTO signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponseDTO("Error: Email is already in use!"));
        }
        if (userRepository.existsByUserName(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDTO("Error: Username is already in use!"));
        }

        Set<String> strRoles = signUpRequest.getRole();
        Set<String> roles = new HashSet<>();

        if (strRoles == null) {
            strRoles = Set.of("user");
        }

        strRoles.forEach(role -> {
            roles.add(role);
        });

        User user = new User(
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            encoder.encode(signUpRequest.getPassword()),
            roles);

        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!"));
    }
}
