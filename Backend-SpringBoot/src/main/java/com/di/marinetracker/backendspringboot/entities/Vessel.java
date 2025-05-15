package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "vessels")
public class Vessel {

    @Id
    private String mmsi;
    private String type;
    private String name;

    @OneToMany(mappedBy = "vessel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp DESC")
    private List<VesselPosition> positions = new ArrayList<>();

    public Vessel(String mmsi, String type) {
        this.mmsi = mmsi;
        this.type = type;
    }

    public Vessel(String mmsi, String type, String name) {
        this.mmsi = mmsi;
        this.type = type;
        this.name = name;
    }

    public Vessel() {
    }

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
}
