package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a specific user, ordered by creation time descending
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find unread notifications for a specific user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    // Find recent notifications for a user (within last X hours)
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("userId") String userId, @Param("since") LocalDateTime since);

    // Check if a similar notification already exists recently (to avoid spam)
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.userId = :userId AND n.vesselMmsi = :vesselMmsi " +
            "AND n.type = :type AND n.createdAt >= :since")
    boolean existsSimilarRecentNotification(@Param("userId") String userId,
                                            @Param("vesselMmsi") String vesselMmsi,
                                            @Param("type") Notification.NotificationType type,
                                            @Param("since") LocalDateTime since);

    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.userId = :userId")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") String userId);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") String userId);

    // Delete old notifications (cleanup)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Count unread notifications for a user
    long countByUserIdAndIsReadFalse(String userId);
}