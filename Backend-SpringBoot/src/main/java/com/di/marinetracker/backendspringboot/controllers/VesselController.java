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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.di.marinetracker.backendspringboot.specifications.VesselSpecifications.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/vessels")
class VesselController {

    private final VesselRepository vesselRepository;
    private final VesselPositionRepository positionRepository;
    private final UserRepository userRepository;

    VesselController(VesselRepository vesselRepository, VesselPositionRepository positionRepository, UserRepository userRepository) {
        this.vesselRepository = vesselRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
    }

    @CrossOrigin(origins ="${cors.urls}")
    @GetMapping("")
    Page<VesselDTO> all(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String type,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "0") int offset,
        Authentication authentication
    ) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by("mmsi").ascending());
        Specification<Vessel> spec = Specification.where(hasType(type)).and(hasName(name));
        Page<Vessel> page = vesselRepository.findAll(spec, pageable);
        List<String> vesselMmsis = page.stream().map(Vessel::getMmsi).toList();

        // we avoid n+1 querying problem this way, could be better
        List<VesselPosition> vesselPositions = vesselRepository.findLatestPositionsByVesselMmsis(vesselMmsis);

        Map<String, VesselPositionDTO> latestPositionMap = new HashMap<>();
        for (VesselPosition vesselPosition : vesselPositions) {
            VesselPositionDTO vp = new VesselPositionDTO(vesselPosition);
            latestPositionMap.put(vesselPosition.getVessel().getMmsi(), vp);
        }

        return page.map(v -> {
            VesselPositionDTO vp = latestPositionMap.get(v.getMmsi());
            return new VesselDTO(v.getMmsi(), v.getName(), v.getType(), vp, userRepository.existsByIdAndFleetMmsi(userId, v.getMmsi()));
        });
    }

    // Single item
    @CrossOrigin(origins ="${cors.urls}")
    @GetMapping("/{mmsi}")
    VesselDTO one(@PathVariable String mmsi, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        Vessel vessel = vesselRepository.findById(mmsi)
            .orElseThrow(() -> new VesselNotFoundException(mmsi));

        VesselPositionDTO latestPosition = positionRepository.findLatestByVesselMmsi(mmsi).map(VesselPositionDTO::new).orElse(null);

        return new VesselDTO(vessel.getMmsi(), vessel.getName(), vessel.getType(), latestPosition, userRepository.existsByIdAndFleetMmsi(userId, vessel.getMmsi()));
    }

    @CrossOrigin(origins ="${cors.urls}")
    @GetMapping("/{mmsi}/path")
    List<VesselPositionDTO> path(@PathVariable String mmsi) {
        List<VesselPositionDTO> positions = new ArrayList<>();

        VesselPositionDTO latestPosition = positionRepository.findLatestByVesselMmsi(mmsi).map(VesselPositionDTO::new).orElse(null);
        if (latestPosition == null) return positions;

        Instant twelveHoursAgo = latestPosition.getTimestamp().minus(12, ChronoUnit.HOURS);
        positions = positionRepository.findPathByMmsi(mmsi, twelveHoursAgo)
                .stream()
                .map(VesselPositionDTO::new)
                .toList();
        return positions;
    }

    @CrossOrigin(origins ="${cors.urls}")
    @PutMapping("/{mmsi}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    VesselDTO updateVessel(@RequestBody Vessel newVessel, @PathVariable String mmsi, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userId = userDetails.getId();

        Vessel savedVessel = vesselRepository.findById(mmsi)
                .map(vessel -> {
                    vessel.setName(newVessel.getName());
                    vessel.setType(newVessel.getType());
                    return vesselRepository.save(vessel);
                })
                .orElseThrow(() -> new VesselNotFoundException(mmsi));

        Optional<VesselPosition> latestPosition = positionRepository.findLatestByVesselMmsi(savedVessel.getMmsi());

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