package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneOfInterestRepository extends JpaRepository<ZoneOfInterest, Long> {
}
