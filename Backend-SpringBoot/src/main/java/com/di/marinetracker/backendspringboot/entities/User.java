package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String userName;
    private String email;
    private String password;
    private Set<String> roles = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "zone_id", referencedColumnName = "id")
    private ZoneOfInterest zoneOfInterest;

    @ManyToMany
    @JoinTable(
            name = "fleets",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "vessel_id")
    )
    @OrderBy("timestamp DESC")
    private List<Vessel> fleet = new ArrayList<>();

    public User(String userName, String email, String password, Set<String> roles) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public User() {
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }

    public String getId() {return id;}

    public String getUserName() {return userName;}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) { this.password = password; }

    public String getPassword() { return password; }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String toString() {
        return "User{" +
                "id=" + id + '\'' +
                "userName=" + userName + '\'' +
                "roles=" + roles + '\'' +
                '}';
    }

    public ZoneOfInterest getZoneOfInterest() {
        return this.zoneOfInterest;
    }

    public void setZoneOfInterest(ZoneOfInterest zoneOfInterest) {
        this.zoneOfInterest = zoneOfInterest;
    }

    public List<Vessel> getFleet() {
        return fleet;
    }

    public void addToFleet(Vessel newVessel) {
        if (!fleet.contains(newVessel)) {
            fleet.add(newVessel);
            newVessel.getUsers().add(this);
        }
    }

    public void removeFromFleet(Vessel vessel) {
        this.fleet.remove(vessel);
        vessel.getUsers().remove(this);
    }
}
