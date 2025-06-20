package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

// Entity representing a user's Zone of Interest (ZoI)
@Entity
@Table(name = "zones_of_interest")
public class ZoneOfInterest {

    // Unique identifier for the ZoI
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Polygon geometry defining the zone area
    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    private Polygon polygon;

    // List of vessel types relevant to this zone
    @ElementCollection
    @CollectionTable(name = "zone_vessel_types", joinColumns = @JoinColumn(name = "zone_id"))
    @Column(name = "vessel_type")
    private List<String> vesselTypes = new ArrayList<>();

    // Maximum allowed vessel speed in the zone
    private Double maxVesselSpeed;

    // User associated with this zone of interest (One-to-One relationship with User)
    @OneToOne(mappedBy = "zoneOfInterest")
    private User user;

    // Default empty constructor (for JPA?)
    public ZoneOfInterest() {
    }

    // Full constructor
    public ZoneOfInterest(Polygon polygon, List<String> vesselTypes, Double maxVesselSpeed) {
        this.polygon = polygon;
        this.vesselTypes = vesselTypes;
        this.maxVesselSpeed = maxVesselSpeed;
    }

    // Getters and setters for the ZoneOfInterest entity fields:

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

    // Method to check if a vessel matches the conditions of this zone of interest
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
