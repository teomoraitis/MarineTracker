package com.di.marinetracker.backendspringboot.vessels;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Vessel {

    private @Id              // mmsi is primary key
    String mmsi; // mmsi
    private String type;     // shiptype
    private String name;

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

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vessel vessel = (Vessel) o;
        return Objects.equals(this.mmsi, vessel.mmsi ) &&
                Objects.equals(this.type, vessel.type) &&
                Objects.equals(this.name, vessel.name);
    }

    @Override
    public String toString() {
        return "Vessel{" +"mmsi='" + mmsi + '\'' + ", type='" + type + '\'' + ", name='" + name + '}';
    }
}
