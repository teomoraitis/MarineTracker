package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;

import com.di.marinetracker.backendspringboot.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    VesselRepository vesselRepository;

    @Transactional
    @CrossOrigin(origins ="${cors.urls}")
    @PostMapping("/{mmsi}")
    public ResponseEntity<?> addVesselToFleet(
        @PathVariable String mmsi,
        Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vessel vessel = vesselRepository.findById(mmsi)
                .orElseThrow(() -> new RuntimeException("Vessel not found"));

        user.addToFleet(vessel);
        userRepository.save(user);

        return ResponseEntity.ok("Vessel added to fleet.");
    }

    @Transactional
    @CrossOrigin(origins ="${cors.urls}")
    @DeleteMapping("/{mmsi}")
    public ResponseEntity<?> removeVesselFromFleet(
            @PathVariable String mmsi,
            Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vessel vessel = vesselRepository.findById(mmsi)
                .orElseThrow(() -> new RuntimeException("Vessel not found"));

        user.removeFromFleet(vessel);
        userRepository.save(user);

        return ResponseEntity.ok("Vessel removed from fleet.");
    }
}
