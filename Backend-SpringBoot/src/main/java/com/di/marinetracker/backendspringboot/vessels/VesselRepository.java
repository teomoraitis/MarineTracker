package com.di.marinetracker.backendspringboot.vessels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VesselRepository extends JpaRepository<Vessel, String>{
    // Define custom query methods if needed
}
