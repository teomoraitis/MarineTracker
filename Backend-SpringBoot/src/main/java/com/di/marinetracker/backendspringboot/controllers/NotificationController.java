package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.entities.Notification;
import com.di.marinetracker.backendspringboot.services.NotificationService;
import com.di.marinetracker.backendspringboot.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Allows cross-origin requests from https://localhost:3000 origin
@CrossOrigin(origins = "${cors.urls}")
// Marks this class as a REST controller and sets the base request mapping
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get notifications for the currently authenticated user.
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        // Get the currently authenticated user's principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String username = userDetails.getUsername();

        List<Notification> notifications = notificationService.getUserNotifications(username, unreadOnly);
        return ResponseEntity.ok(notifications);
    }

    // Gets the count of unread notifications for the current user.
    @GetMapping("/count/unread")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount() {
        // Get the currently authenticated user's principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String username = userDetails.getUsername();

        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Marks a specific notification as read.
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        // Get the currently authenticated user's principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String username = userDetails.getUsername();

        boolean success = notificationService.markAsRead(notificationId, username);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            // Notification not found or user does not have permission
            return ResponseEntity.notFound().build();
        }
    }

    // Marks all unread notifications for the current user as read.
    @PutMapping("/read/all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        // Get the currently authenticated user's principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String username = userDetails.getUsername();

        int updatedCount = notificationService.markAllAsRead(username);
        return ResponseEntity.ok(Map.of("updatedCount", updatedCount));
    }
}