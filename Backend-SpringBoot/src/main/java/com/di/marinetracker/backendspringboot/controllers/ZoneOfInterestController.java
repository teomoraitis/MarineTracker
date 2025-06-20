package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.dto.ZoneOfInterestDTO;
import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.repositories.ZoneOfInterestRepository;
import com.di.marinetracker.backendspringboot.services.UserDetailsImpl;

import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// Allows cross-origin requests from https://localhost:3000 origin
@CrossOrigin(origins = "${cors.urls}")
// Marks this class as a REST controller and sets the base request mapping
@RestController
@RequestMapping("/api/zone")
public class ZoneOfInterestController {

    // Injects the ZoneOfInterestRepository for zoi data access
    @Autowired
    ZoneOfInterestRepository zoneRepository;

    // Injects the UserRepository for user data access
    @Autowired
    UserRepository userRepository;

    // POST endpoint to create or update a user's Zone of Interest
    @PostMapping
    public ResponseEntity<?> createOrUpdateZone(@RequestBody ZoneOfInterestDTO dto) {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        try {
            // Convert WKT string into a JTS Polygon
            WKTReader reader = new WKTReader();
            var polygon = (org.locationtech.jts.geom.Polygon) reader.read(dto.getPolygonWKT());

            // Check if user already has a zone
            ZoneOfInterest zone = user.getZoneOfInterest();
            if (zone == null) {
                zone = new ZoneOfInterest();
            }

            // Set properties from DTO
            zone.setPolygon(polygon);
            zone.setVesselTypes(dto.getVesselTypes());
            zone.setMaxVesselSpeed(dto.getMaxVesselSpeed());
            zone.setUser(user);

            // Save zone
            zoneRepository.save(zone);

            // Save/Update user's reference to the zone
            user.setZoneOfInterest(zone);
            userRepository.save(user);

            return ResponseEntity.ok("Zone of Interest saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid polygon or data: " + e.getMessage());
        }
    }

    // GET endpoint to retrieve the current user's Zone of Interest
    @GetMapping
    public ResponseEntity<?> getZone() {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        // Check if user has a Zone of Interest and retrieve it
        ZoneOfInterest zone = user.getZoneOfInterest();
        if (zone == null) {
            return ResponseEntity.notFound().build();
        }

        // Convert entity to DTO
        ZoneOfInterestDTO dto = new ZoneOfInterestDTO();
        dto.setPolygonWKT(zone.getPolygon().toText());
        dto.setVesselTypes(zone.getVesselTypes());
        dto.setMaxVesselSpeed(zone.getMaxVesselSpeed());

        // Return the Zone of Interest DTO
        return ResponseEntity.ok(dto);
    }


    // DELETE endpoint to remove the current user's Zone of Interest
    @DeleteMapping
    public ResponseEntity<?> deleteZone() {
        // Get the currently authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        // Check if user has a Zone of Interest and retrieve it
        ZoneOfInterest zone = user.getZoneOfInterest();
        if (zone == null) {
            return ResponseEntity.notFound().build();
        }

        // Remove zone and unlink from user
        zoneRepository.delete(zone);
        user.setZoneOfInterest(null);
        userRepository.save(user);

        return ResponseEntity.ok("Zone of Interest deleted");
    }
}
