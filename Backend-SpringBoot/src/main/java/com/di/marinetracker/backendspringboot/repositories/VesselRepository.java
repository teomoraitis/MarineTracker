package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VesselRepository extends JpaRepository<Vessel, String> {
    // The primary key type is String because we're using MMSI as the ID
}
