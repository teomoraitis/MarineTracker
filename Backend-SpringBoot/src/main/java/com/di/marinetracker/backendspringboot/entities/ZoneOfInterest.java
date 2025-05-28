package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zones_of_interest")
public class ZoneOfInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    private Polygon polygon;

    @ElementCollection
    @CollectionTable(name = "zone_vessel_types", joinColumns = @JoinColumn(name = "zone_id"))
    @Column(name = "vessel_type")
    private List<String> vesselTypes = new ArrayList<>();

    private Double maxVesselSpeed;

    @OneToOne(mappedBy = "zoneOfInterest")
    private User user;

    public ZoneOfInterest() {
    }

    public ZoneOfInterest(Polygon polygon, List<String> vesselTypes, Double maxVesselSpeed) {
        this.polygon = polygon;
        this.vesselTypes = vesselTypes;
        this.maxVesselSpeed = maxVesselSpeed;
    }

    public Long getId() {
        return id;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public List<String> getVesselTypes() {
        return vesselTypes;
    }

    public void setVesselTypes(List<String> vesselTypes) {
        this.vesselTypes = vesselTypes;
    }

    public Double getMaxVesselSpeed() {
        return maxVesselSpeed;
    }

    public void setMaxVesselSpeed(Double maxVesselSpeed) {
        this.maxVesselSpeed = maxVesselSpeed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ZoneOfInterest{" +
                "id=" + id +
                ", polygon=" + polygon +
                ", vesselTypes=" + vesselTypes +
                ", maxVesselSpeed=" + maxVesselSpeed +
                '}';
    }

    public boolean matchesConditions(Vessel vessel) {
        if (vessel == null) return false;

        VesselPosition vesselPosition = vessel.getLatestPosition();
        Point location = vesselPosition.getLocation();
        String type = vessel.getType();
        Double speed = vesselPosition.getSpeed();

        if (location == null || type == null || speed == null) return false;

        return polygon.contains(location)
                && vesselTypes.contains(type)
                && (maxVesselSpeed == null || speed <= maxVesselSpeed);
    }
}
