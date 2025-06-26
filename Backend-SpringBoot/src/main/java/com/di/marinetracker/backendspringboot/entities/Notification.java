package com.di.marinetracker.backendspringboot.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String vesselMmsi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime timestamp; // When the event actually occurred

    // Additional context data
    private Double vesselSpeed;
    private Double vesselLatitude;
    private Double vesselLongitude;

    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification(String userId, String vesselMmsi, NotificationType type,
                        String message, LocalDateTime timestamp) {
        this();
        this.userId = userId;
        this.vesselMmsi = vesselMmsi;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getVesselMmsi() { return vesselMmsi; }
    public void setVesselMmsi(String vesselMmsi) { this.vesselMmsi = vesselMmsi; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Double getVesselSpeed() { return vesselSpeed; }
    public void setVesselSpeed(Double vesselSpeed) { this.vesselSpeed = vesselSpeed; }

    public Double getVesselLatitude() { return vesselLatitude; }
    public void setVesselLatitude(Double vesselLatitude) { this.vesselLatitude = vesselLatitude; }

    public Double getVesselLongitude() { return vesselLongitude; }
    public void setVesselLongitude(Double vesselLongitude) { this.vesselLongitude = vesselLongitude; }

    // Enum for notification types
    public enum NotificationType {
        ZONE_ENTRY("Zone Entry"),
        ZONE_EXIT("Zone Exit"),
        SPEED_VIOLATION("Speed Violation"),
        FLEET_ALERT("Fleet Alert");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}