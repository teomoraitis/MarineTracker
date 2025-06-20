package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import java.time.Instant;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

// Entity representing a Vessel's Position report
@Entity
@Table(name = "vessel_positions")
public class VesselPosition {

    // Unique identifier for the position report
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vessel associated with this position report (Many-to-One relationship with Vessel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_mmsi", nullable = false)
    private Vessel vessel;

    // Geographical coordinates of the position report:
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double course;
    private Integer status;
    private Double turn;
    private Integer heading;
    private Instant timestamp;

    // Default empty constructor (for JPA?)
    public VesselPosition() {
    }

    // Full constructor
    public VesselPosition(Vessel vessel, Double latitude, Double longitude, Double speed, Double course, 
                         Integer status, Double turn, Integer heading, Instant timestamp) {
        this.vessel = vessel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.course = course;
        this.status = status;
        this.turn = turn;
        this.heading = heading;
        this.timestamp = timestamp;
    }

    // Getters and setters for the VesselPosition entity fields:

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vessel getVessel() {
        return vessel;
    }

    public void setVessel(Vessel vessel) {
        this.vessel = vessel;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
        this.course = course;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getTurn() {
        return turn;
    }

    public void setTurn(Double turn) {
        this.turn = turn;
    }

    public Integer getHeading() {
        return heading;
    }

    public void setHeading(Integer heading) {
        this.heading = heading;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Point getLocation() {

        GeometryFactory geometryFactory = new GeometryFactory();
        return geometryFactory.createPoint(new Coordinate(this.longitude, this.latitude));
    }
}
