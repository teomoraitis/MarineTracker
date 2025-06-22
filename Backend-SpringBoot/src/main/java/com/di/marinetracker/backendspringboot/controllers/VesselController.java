package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.dto.VesselPositionDTO;
import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.dto.VesselDTO;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.exceptions.VesselNotFoundException;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselPositionRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.di.marinetracker.backendspringboot.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.JsonNode;
import org.geolatte.geom.V;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.di.marinetracker.backendspringboot.specifications.VesselSpecifications.*;

// Allows cross-origin requests from any origin with a max age of 3600 seconds
@CrossOrigin(origins = "*", maxAge = 3600)
// Marks this class as a REST controller and sets the base request mapping
@RestController
@RequestMapping("/api/vessels")
class VesselController {

    private final VesselRepository vesselRepository;
    private final VesselPositionRepository positionRepository;
    private final UserRepository userRepository;

    @Autowired
    private VesselCacheController vesselDataCache;

    // Constructor injection for repositories (instead of using @Autowired)
    VesselController(VesselRepository vesselRepository, VesselPositionRepository positionRepository, UserRepository userRepository) {
        this.vesselRepository = vesselRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
    }

    // Returns a paginated list of vessels with optional filters, when a GET request is made to /api/vessels
    @CrossOrigin(origins ="${cors.urls}")
    @GetMapping("")
    Page<VesselDTO> all(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int offset,
        Authentication authentication
    ) {
        // Get authenticated user ID
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Set up pagination and filtering
        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("mmsi").ascending());
        Specification<Vessel> spec = Specification.where(hasType(type)).and(hasName(name));
        Page<Vessel> page = vesselRepository.findAll(spec, pageable);

        // Get MMSIs for vessels in the page
        List<String> vesselMmsis = page.stream().map(Vessel::getMmsi).toList();

        // Fetch latest positions for vessels,
        // we avoid n+1 querying problem this way, could be better
        List<VesselPosition> vesselPositions = vesselRepository.findLatestPositionsByVesselMmsis(vesselMmsis);

        // Map vessel MMSI to latest position DTO
        Map<String, VesselPositionDTO> latestPositionMap = new HashMap<>();
        for (VesselPosition vesselPosition : vesselPositions) {
            VesselPositionDTO vp = new VesselPositionDTO(vesselPosition);
            latestPositionMap.put(vesselPosition.getVessel().getMmsi(), vp);
        }

        // Map vessels to DTOs including latest position and fleet status
        return page.map(v -> {
            VesselPositionDTO vp = latestPositionMap.get(v.getMmsi());
            return new VesselDTO(v.getMmsi(), v.getName(), v.getType(), vp, userRepository.existsByIdAndFleetMmsi(userId, v.getMmsi()));
        });
    }

    // Single item (vessel) return when a GET request is made to /api/vessels/{mmsi}
    @CrossOrigin(origins ="${cors.urls}")
    @GetMapping("/{mmsi}")
    VesselDTO one(@PathVariable String mmsi, Authentication authentication) {
        // Get authenticated user ID
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Find vessel or throw if not found
        Vessel vessel = vesselRepository.findById(mmsi)
            .orElseThrow(() -> new VesselNotFoundException(mmsi));

        // Get latest position for vessel
        VesselPositionDTO latestPosition = positionRepository.findLatestByVesselMmsi(mmsi).map(VesselPositionDTO::new).orElse(null);

        // Return vessel DTO with position and fleet status
        return new VesselDTO(vessel.getMmsi(), vessel.getName(), vessel.getType(), latestPosition, userRepository.existsByIdAndFleetMmsi(userId, vessel.getMmsi()));
    }

    // Returns the path (positions) for a vessel in the last 12 hours, when a GET request is made to /api/vessels/{mmsi}/path
    @CrossOrigin(origins ="${cors.urls}")
    @GetMapping("/{mmsi}/path")
    List<VesselPositionDTO> path(@PathVariable String mmsi) {
        List<VesselPositionDTO> positions = new ArrayList<>();

        // Get latest position for time reference
        VesselPositionDTO latestPosition = positionRepository.findLatestByVesselMmsi(mmsi).map(VesselPositionDTO::new).orElse(null);
        if (latestPosition == null) return positions;

        // Calculate timestamp for 12 hours ago
        Instant twelveHoursAgo = latestPosition.getTimestamp().minus(12, ChronoUnit.HOURS);

        // Fetch positions since 12 hours ago
        positions = positionRepository.findPathByMmsi(mmsi, twelveHoursAgo)
                .stream()
                .map(VesselPositionDTO::new)
                .toList();
        return positions;
    }

    @CrossOrigin(origins = "${cors.urls}")
    @GetMapping("/reload")
    List <VesselDTO> reload() {
        List<VesselDTO> currentVessels = new ArrayList<>();

        vesselDataCache.forEachVesselPosition((mmsi, vesselData) -> {
            VesselPositionDTO vesselPositionDTO = new VesselPositionDTO(vesselData);
            Vessel vessel = vesselData.getVessel();
            VesselDTO vesselDTO = new VesselDTO(mmsi, vessel.getName(), vessel.getType(), vesselPositionDTO, false);
            currentVessels.add(vesselDTO);
        });
        return currentVessels;
    }

    // Updates vessel details (admin only), when a PUT request is made to /api/vessels/{mmsi}
    @CrossOrigin(origins ="${cors.urls}")
    @PutMapping("/{mmsi}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    VesselDTO updateVessel(@RequestBody Vessel newVessel, @PathVariable String mmsi, Authentication authentication) {
        // Get authenticated user ID
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        // Update vessel if found, else throw exception
        Vessel savedVessel = vesselRepository.findById(mmsi)
                .map(vessel -> {
                    vessel.setName(newVessel.getName());
                    vessel.setType(newVessel.getType());
                    return vesselRepository.save(vessel);
                })
                .orElseThrow(() -> new VesselNotFoundException(mmsi));

        // Get latest position for updated vessel
        Optional<VesselPosition> latestPosition = positionRepository.findLatestByVesselMmsi(savedVessel.getMmsi());

        // Return updated vessel DTO
        VesselDTO vesselDTO = new VesselDTO(
                savedVessel.getMmsi(),
                savedVessel.getName(),
                savedVessel.getType(),
                latestPosition.map(VesselPositionDTO::new).orElseThrow(() -> new VesselNotFoundException(mmsi)),
                userRepository.existsByIdAndFleetMmsi(userId, savedVessel.getMmsi())
        );

        return vesselDTO;
    }

}