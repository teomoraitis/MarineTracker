package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/signup")
public class UserController {
    @Autowired
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // signup
    @CrossOrigin(origins = "${cors.urls}")
    @PostMapping(path = "/signup", produces = "application/json")
    public ResponseEntity<JsonNode> signUp(@RequestBody JsonNode jsonNode) {
        // TODO check if user already exists. Maybe via username / email?
        String userName = jsonNode.get("username").textValue();
        String password = jsonNode.get("password").textValue();
        User newUser = new User(userName, password, "user");
        userRepository.save(newUser);
        return new ResponseEntity<>(jsonNode,HttpStatus.CREATED);
    }
}