package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Entity representing a Vessel
@Entity
@Table(name = "vessels")
public class Vessel {

    // Unique identifier for the vessel (MMSI)
    @Id
    private String mmsi;

    // Type of the vessel
    private String type;

    // Name of the vessel
    private String name;

    // List of position reports for the vessel, ordered by timestamp descending (One-to-Many relationship with VesselPosition)
    @OneToMany(mappedBy = "vessel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp DESC")
    private List<VesselPosition> positions = new ArrayList<>();

    // List of users who have this vessel in their fleet (Many-to-Many relationship with User)
    @ManyToMany(mappedBy = "fleet")
    private List<User> users = new ArrayList<>();

    // Constructor for creating a Vessel with MMSI and type
    public Vessel(String mmsi, String type) {
        this.mmsi = mmsi;
        this.type = type;
    }

    // Constructor for creating a Vessel with MMSI, type, and name
    public Vessel(String mmsi, String type, String name) {
        this.mmsi = mmsi;
        this.type = type;
        this.name = name;
    }

    // Default empty constructor (for JPA?)
    public Vessel() {
    }

    // Getters and setters for the Vessel entity fields:

    public String getMmsi() {
        return mmsi;
    }

    public void setMmsi(String mmsi) {
        this.mmsi = mmsi;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<VesselPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<VesselPosition> positions) {
        this.positions = positions;
    }

    public void addPosition(VesselPosition position) {
        positions.add(position);
        position.setVessel(this);
    }

    public VesselPosition getLatestPosition() {
        return positions.isEmpty() ? null : positions.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vessel vessel = (Vessel) o;
        return Objects.equals(mmsi, vessel.mmsi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mmsi);
    }

    @Override
    public String toString() {
        return "Vessel{" +
                "mmsi='" + mmsi + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public List<User> getUsers() {
        return users;
    }

}
