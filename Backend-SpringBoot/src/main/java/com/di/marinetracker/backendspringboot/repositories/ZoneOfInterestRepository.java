package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Repository interface for ZoneOfInterest entity
public interface ZoneOfInterestRepository extends JpaRepository<ZoneOfInterest, Long> {

    @Query("""
        select z from ZoneOfInterest z
        left join fetch z.vesselTypes
        where z.user.id = :userId
    """)
    ZoneOfInterest findByUserIdWithVesselTypes(@Param("userId") String userId);
}
