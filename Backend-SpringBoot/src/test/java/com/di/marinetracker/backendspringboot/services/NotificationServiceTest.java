package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Notification;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import com.di.marinetracker.backendspringboot.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationService notificationService;

    @Test
    void testNullValues_noException() {
        List<Notification> notificationList = notificationService.generateZoneNotifications(null, null, null, null);
        assert notificationList.isEmpty();
    }

    @Test
    void testNullZone_noException() {
        ZoneOfInterest zone = new ZoneOfInterest();
        Vessel vessel = Mockito.mock(Vessel.class);
        Mockito.when(vessel.getLatestPosition()).thenReturn(null);
        List<Notification> notificationList = notificationService.generateZoneNotifications(vessel, "test-user-id", zone, LocalDateTime.now());
        assert notificationList.isEmpty();
    }

    @Test
    void testShipAppearsInZone_notifies() {
        Vessel vessel = Mockito.mock(Vessel.class);
        ZoneOfInterest zone = Mockito.mock(ZoneOfInterest.class);
        Mockito.when(vessel.getMmsi()).thenReturn("test-mmsi");
        Mockito.when(zone.matchesConditions(vessel)).thenReturn(true);
        Mockito.when(zone.getMaxVesselSpeed()).thenReturn(3.0);
        VesselPosition position = Mockito.mock(VesselPosition.class);
        Mockito.when(position.getSpeed()).thenReturn(2.0);
        Mockito.when(position.getTimestamp()).thenReturn(Instant.now());
        Mockito.when(vessel.getLatestPosition()).thenReturn(position);
        Mockito.when(notificationRepository.existsSimilarRecentNotification(
                eq("test-user-id"), eq("test-mmsi"), eq(Notification.NotificationType.ZONE_ENTRY), any()))
                .thenReturn(false);
        LocalDateTime timeNow = LocalDateTime.now();
        List<Notification> notificationList = notificationService.generateZoneNotifications(vessel, "test-user-id", zone, timeNow);
        assert notificationList.size() == 1;
    }

    @Test
    void testShipAppearsInZoneAndFast_notifiesTwice() {
        Vessel vessel = Mockito.mock(Vessel.class);
        ZoneOfInterest zone = Mockito.mock(ZoneOfInterest.class);
        Mockito.when(vessel.getMmsi()).thenReturn("test-mmsi");
        Mockito.when(zone.matchesConditions(vessel)).thenReturn(true);
        Mockito.when(zone.getMaxVesselSpeed()).thenReturn(3.0);
        VesselPosition position = Mockito.mock(VesselPosition.class);
        Mockito.when(position.getSpeed()).thenReturn(4.0);
        Mockito.when(position.getTimestamp()).thenReturn(Instant.now());
        Mockito.when(vessel.getLatestPosition()).thenReturn(position);
        Mockito.when(notificationRepository.existsSimilarRecentNotification(
                        eq("test-user-id"), eq("test-mmsi"), any(), any()))
                .thenReturn(false);
        LocalDateTime timeNow = LocalDateTime.now();
        List<Notification> notificationList = notificationService.generateZoneNotifications(vessel, "test-user-id", zone, timeNow);
        assert notificationList.size() == 2;
    }

    @Test
    void testShipStaysInZone_noNotification() {
        Vessel vessel = Mockito.mock(Vessel.class);
        ZoneOfInterest zone = Mockito.mock(ZoneOfInterest.class);
        Mockito.when(vessel.getMmsi()).thenReturn("test-mmsi");
        Mockito.when(zone.matchesConditions(vessel)).thenReturn(true);
        Mockito.when(zone.getMaxVesselSpeed()).thenReturn(3.0);
        VesselPosition position = Mockito.mock(VesselPosition.class);
        Mockito.when(position.getSpeed()).thenReturn(2.0);
        Mockito.when(position.getTimestamp()).thenReturn(Instant.now());
        Mockito.when(vessel.getLatestPosition()).thenReturn(position);
        Mockito.when(notificationRepository.existsSimilarRecentNotification(
                        eq("test-user-id"), eq("test-mmsi"), any(), any()))
                .thenReturn(true);
        LocalDateTime timeNow = LocalDateTime.now();
        List<Notification> notificationList = notificationService.generateZoneNotifications(vessel, "test-user-id", zone, timeNow);
        assert notificationList.isEmpty();
    }
}
