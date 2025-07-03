package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Notification;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import com.di.marinetracker.backendspringboot.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final int NOTIFICATION_COOLDOWN_MINUTES = 15; // Prevent spam

    @Autowired
    private NotificationRepository notificationRepository;

    // Track vessels that are currently in zones to detect exits
    private final Map<String, Map<String, Boolean>> vesselZoneStatus = new ConcurrentHashMap<>();
    // Key: userId, Value: Map of vesselMmsi -> isInZone

    /**
     * Generate notifications for a vessel based on user's zone of interest
     */
    @Transactional
    public List<Notification> generateZoneNotifications(Vessel vessel, String userId, ZoneOfInterest zone) {
        // Tests can pass a different time argument, to test the cooldown timer
        return generateZoneNotifications(vessel, userId, zone, LocalDateTime.now());
    }
    public List<Notification> generateZoneNotifications(Vessel vessel, String userId, ZoneOfInterest zone, LocalDateTime timeNow) {
        List<Notification> newNotifications = new ArrayList<>();

        if (zone == null || vessel == null) {
            return newNotifications;
        }

        VesselPosition position = vessel.getLatestPosition();
        if (position == null) {
            return newNotifications;
        }

        String vesselMmsi = vessel.getMmsi();
        boolean currentlyInZone = zone.matchesConditions(vessel);
        boolean wasInZone = wasVesselInZone(userId, vesselMmsi);

        System.out.println("before if");
        // Zone Entry Detection
        if (currentlyInZone && !wasInZone) {
            System.out.println("first check passed");
            Notification notification = createZoneEntryNotification(vessel, userId, position);
            if (shouldCreateNotification(userId, vesselMmsi, Notification.NotificationType.ZONE_ENTRY, timeNow)) {
                newNotifications.add(notificationRepository.save(notification));
                logger.info("Created zone entry notification for vessel {} and user {}", vesselMmsi, userId);
            }
        }

        // Zone Exit Detection
        if (!currentlyInZone && wasInZone) {
            Notification notification = createZoneExitNotification(vessel, userId, position);
            if (shouldCreateNotification(userId, vesselMmsi, Notification.NotificationType.ZONE_EXIT, timeNow)) {
                newNotifications.add(notificationRepository.save(notification));
                logger.info("Created zone exit notification for vessel {} and user {}", vesselMmsi, userId);
            }
        }

        // Speed Violation Detection (if vessel is in zone)
        if (currentlyInZone && zone.getMaxVesselSpeed() != null &&
                position.getSpeed() != null && position.getSpeed() > zone.getMaxVesselSpeed()) {

            Notification notification = createSpeedViolationNotification(vessel, userId, position, zone.getMaxVesselSpeed());
            if (shouldCreateNotification(userId, vesselMmsi, Notification.NotificationType.SPEED_VIOLATION, timeNow)) {
                newNotifications.add(notificationRepository.save(notification));
                logger.info("Created speed violation notification for vessel {} and user {}", vesselMmsi, userId);
            }
        }

        // Update vessel zone status
        updateVesselZoneStatus(userId, vesselMmsi, currentlyInZone);

        return newNotifications;
    }

    private Notification createZoneEntryNotification(Vessel vessel, String userId, VesselPosition position) {
        String message = String.format("Vessel %s (%s) entered your zone of interest",
                vessel.getName() != null ? vessel.getName() : "Unknown",
                vessel.getMmsi());

        Notification notification = new Notification(userId, vessel.getMmsi(),
                Notification.NotificationType.ZONE_ENTRY,
                message, position.getTimestamp().atZone(java.time.ZoneOffset.UTC).toLocalDateTime());

        notification.setVesselSpeed(position.getSpeed());
        notification.setVesselLatitude(position.getLatitude());
        notification.setVesselLongitude(position.getLongitude());

        return notification;
    }

    private Notification createZoneExitNotification(Vessel vessel, String userId, VesselPosition position) {
        String message = String.format("Vessel %s (%s) exited your zone of interest",
                vessel.getName() != null ? vessel.getName() : "Unknown",
                vessel.getMmsi());

        Notification notification = new Notification(userId, vessel.getMmsi(),
                Notification.NotificationType.ZONE_EXIT,
                message, position.getTimestamp().atZone(java.time.ZoneOffset.UTC).toLocalDateTime());

        notification.setVesselSpeed(position.getSpeed());
        notification.setVesselLatitude(position.getLatitude());
        notification.setVesselLongitude(position.getLongitude());

        return notification;
    }

    private Notification createSpeedViolationNotification(Vessel vessel, String userId, VesselPosition position, Double maxSpeed) {
        String message = String.format("Vessel %s (%s) is exceeding speed limit: %.1f knots (max: %.1f knots)",
                vessel.getName() != null ? vessel.getName() : "Unknown",
                vessel.getMmsi(), position.getSpeed(), maxSpeed);

        Notification notification = new Notification(userId, vessel.getMmsi(),
                Notification.NotificationType.SPEED_VIOLATION,
                message, position.getTimestamp().atZone(java.time.ZoneOffset.UTC).toLocalDateTime());

        notification.setVesselSpeed(position.getSpeed());
        notification.setVesselLatitude(position.getLatitude());
        notification.setVesselLongitude(position.getLongitude());

        return notification;
    }

    private boolean shouldCreateNotification(String userId, String vesselMmsi, Notification.NotificationType type, LocalDateTime timeNow) {
        LocalDateTime cutoff = timeNow.minusMinutes(NOTIFICATION_COOLDOWN_MINUTES);
        return !notificationRepository.existsSimilarRecentNotification(userId, vesselMmsi, type, cutoff);
    }

    private boolean wasVesselInZone(String userId, String vesselMmsi) {
        return vesselZoneStatus.getOrDefault(userId, new ConcurrentHashMap<>())
                .getOrDefault(vesselMmsi, false);
    }

    private void updateVesselZoneStatus(String userId, String vesselMmsi, boolean inZone) {
        vesselZoneStatus.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(vesselMmsi, inZone);
    }

    /**
     * Get notifications for a user
     */
    public List<Notification> getUserNotifications(String userId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public boolean markAsRead(Long notificationId, String userId) {
        return notificationRepository.markAsRead(notificationId, userId) > 0;
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(String userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    /**
     * Get unread notification count
     */
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Cleanup old notifications (should be called periodically)
     */
    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        return notificationRepository.deleteOldNotifications(cutoff);
    }
}