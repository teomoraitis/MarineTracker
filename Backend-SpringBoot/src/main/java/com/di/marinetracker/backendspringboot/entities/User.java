package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private String id;
    private String userName;
    private String hashedPassword;
    private String role;
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon zoneOfInterest;


    @ManyToMany
    @JoinTable(
            name = "fleets",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "vessel_id")
    )
    @OrderBy("timestamp DESC")
    private List<Vessel> fleet = new ArrayList<>();

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public User(String userName, String password, String role) {
        this.userName = userName;
        this.hashedPassword = passwordEncoder().encode(password);
        this.role = role;
    }

    public User() {
    }

    public String getId() {return id;}

    public String getUserName() {return userName;}

    public void setPassword(String password) { this.hashedPassword = passwordEncoder().encode(password); }

    public String getRole() {return role;}

    public boolean passwordMatch(String password) {
        return (passwordEncoder().matches(password, this.hashedPassword));
    }

    public String toString() {
        return "User{" +
                "id=" + id + '\'' +
                "userName=" + userName + '\'' +
                "role=" + role + '\'' +
                '}';
    }

    public Polygon getZoneOfInterest() {
        return zoneOfInterest;
    }

    public void setZoneOfInterest(Polygon zoneOfInterest) {
        this.zoneOfInterest = zoneOfInterest;
    }
}
