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

// Allows cross-origin requests from any origin with a max age of 3600 seconds
@CrossOrigin(origins = "*", maxAge = 3600)
// Marks this class as a REST controller and sets the base request mapping
@RestController
@RequestMapping("/api/fleet")
public class FleetController {

    // Injects the UserRepository for user data access
    @Autowired
    UserRepository userRepository;

    // Injects the VesselRepository for vessel data access
    @Autowired
    VesselRepository vesselRepository;

    // Adds a vessel to the authenticated user's fleet when a POST request is made to /api/fleet/{mmsi}
    @Transactional
    @CrossOrigin(origins ="${cors.urls}")
    @PostMapping("/{mmsi}")
    public ResponseEntity<?> addVesselToFleet(@PathVariable String mmsi, Authentication authentication) {
        // Get authenticated user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find vessel by MMSI
        Vessel vessel = vesselRepository.findById(mmsi)
                .orElseThrow(() -> new RuntimeException("Vessel not found"));

        // Add vessel to user's fleet and save the user
        user.addToFleet(vessel);
        userRepository.save(user);

        // Return success response
        return ResponseEntity.ok("Vessel added to fleet.");
    }

    // Removes a vessel from the authenticated user's fleet when a DELETE request is made to /api/fleet/{mmsi}
    @Transactional
    @CrossOrigin(origins ="${cors.urls}")
    @DeleteMapping("/{mmsi}")
    public ResponseEntity<?> removeVesselFromFleet(@PathVariable String mmsi, Authentication authentication) {
        // Get authenticated user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find vessel by MMSI
        Vessel vessel = vesselRepository.findById(mmsi)
                .orElseThrow(() -> new RuntimeException("Vessel not found"));

        // Remove vessel from user's fleet and save the user
        user.removeFromFleet(vessel);
        userRepository.save(user);

        // Return success response
        return ResponseEntity.ok("Vessel removed from fleet.");
    }
}
