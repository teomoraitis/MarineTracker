package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository interface for Vessel entity
@Repository
public interface VesselRepository extends JpaRepository<Vessel, String>, JpaSpecificationExecutor<Vessel> {
    // Find the latest position report for each vessel in the given list of MMSIs
    @Query(value = """
        SELECT vp
        FROM VesselPosition vp
        WHERE vp.vessel.mmsi IN :mmsis
          AND vp.timestamp = (
              SELECT MAX(vp2.timestamp)
              FROM VesselPosition vp2
              WHERE vp2.vessel.mmsi = vp.vessel.mmsi
          )
    """)
    List<VesselPosition> findLatestPositionsByVesselMmsis(@Param("mmsis") List<String> mmsis);

}
