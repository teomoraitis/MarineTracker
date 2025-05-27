package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // signup
    @CrossOrigin(origins = "${cors.urls}")
    @PostMapping(path = "/api/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> signUp(@RequestBody String string) throws JsonProcessingException {
        JsonNode jsonNode = new ObjectMapper().readTree(string);
        // TODO check if user already exists. Maybe via username / email?
        String userName = jsonNode.get("username").textValue();
        String password = jsonNode.get("password").textValue();
        User newUser = new User(userName, password, "user");
        userRepository.save(newUser);
        return new ResponseEntity<>(string, HttpStatus.CREATED);
    }
}