package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VesselPositionRepository extends JpaRepository<VesselPosition, Long> {
    List<VesselPosition> findByVesselMmsiOrderByTimestampDesc(String mmsi);
    
    @Query("SELECT vp FROM VesselPosition vp WHERE vp.vessel.mmsi = :mmsi ORDER BY vp.timestamp DESC LIMIT 1")
    Optional<VesselPosition> findLatestByVesselMmsi(String mmsi);
}
