package com.di.marinetracker.backendspringboot.dto;

import com.di.marinetracker.backendspringboot.entities.VesselPosition;

import java.time.Instant;

// DTO for transferring vessel position data between backend and client
public class VesselPositionDTO {
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double course;
    private Integer status;
    private Double turn;
    private Integer heading;
    private Instant timestamp;

    public VesselPositionDTO(VesselPosition position) {
        this.latitude = position.getLatitude();
        this.longitude = position.getLongitude();
        this.speed = position.getSpeed();
        this.course = position.getCourse();
        this.status = position.getStatus();
        this.turn = position.getTurn();
        this.heading = position.getHeading();
        this.timestamp = position.getTimestamp();
    }

    public VesselPositionDTO(
        Double latitude,
        Double longitude,
        Double speed,
        Double course,
        Integer status,
        Double turn,
        Integer heading,
        Instant timestamp
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.course = course;
        this.status = status;
        this.turn = turn;
        this.heading = heading;
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public Double getCourse() {
        return course;
    }

    public Integer getStatus() {
        return status;
    }

    public Double getTurn() {
        return turn;
    }

    public Integer getHeading() {
        return heading;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    // Getters and setters (or use Lombok)
}