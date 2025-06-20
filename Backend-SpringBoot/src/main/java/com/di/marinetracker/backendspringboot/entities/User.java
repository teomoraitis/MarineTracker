package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Entity representing a User
@Entity
@Table(name = "users")
public class User {
    // Unique identifier for the user (UUID)
    @Id
    private String id;

    // Username for authentication and display
    private String userName;

    // User's email address
    private String email;

    // Encrypted user password
    private String password;

    // Set of roles assigned to the user
    private Set<String> roles = new HashSet<>();

    // User's zone of interest (One-to-One relationship with ZoneOfInterest)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "zone_id", referencedColumnName = "id")
    private ZoneOfInterest zoneOfInterest;

    // List of vessels in the user's fleet (Many-to-Many relationship with Vessel)
    @ManyToMany
    @JoinTable(
            name = "fleets",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "vessel_id")
    )
    private List<Vessel> fleet = new ArrayList<>();

    // Constructor for creating a User with all fields
    public User(String userName, String email, String password, Set<String> roles) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    // Default empty constructor (for JPA?)
    public User() {
    }

    // Generates a unique ID for the user before persisting to the database
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }

    // Getters and Setters for the User entity fields:

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

    // Adds a new vessel to the user's fleet and updates the vessel's users list
    public void addToFleet(Vessel newVessel) {
        if (!fleet.contains(newVessel)) {
            fleet.add(newVessel);
            newVessel.getUsers().add(this);
        }
    }

    // Removes a vessel from the user's fleet and updates the vessel's users list
    public void removeFromFleet(Vessel vessel) {
        this.fleet.remove(vessel);
        vessel.getUsers().remove(this);
    }
}
