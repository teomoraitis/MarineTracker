package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// Repository interface for VesselPosition entity
@Repository
public interface VesselPositionRepository extends JpaRepository<VesselPosition, Long> {
    // Find all position reports for a vessel, ordered by timestamp descending
    List<VesselPosition> findByVesselMmsiOrderByTimestampDesc(String mmsi);

    // Find the latest position report for a vessel by MMSI
    @Query("SELECT vp FROM VesselPosition vp WHERE vp.vessel.mmsi = :mmsi ORDER BY vp.timestamp DESC LIMIT 1")
    Optional<VesselPosition> findLatestByVesselMmsi(String mmsi);

    // Find the two latest position reports for a vessel by MMSI
    @Query("SELECT vp FROM VesselPosition vp WHERE vp.vessel.mmsi = :mmsi ORDER BY vp.timestamp DESC LIMIT 2")
    Optional<VesselPosition[]> find2LatestByVesselMmsi(String mmsi);

    // Find all position reports for a vessel after a cutoff time, ordered by timestamp descending
    @Query("""
        SELECT vp
        FROM VesselPosition vp
        WHERE
            vp.vessel.mmsi = :mmsi
            AND vp.timestamp >= :cutoffTime
        ORDER BY vp.timestamp DESC
    """)
    List<VesselPosition> findPathByMmsi(@Param("mmsi")String mmsi, @Param("cutoffTime") Instant cutoffTime);
}
