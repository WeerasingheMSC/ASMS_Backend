package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all notifications for logged-in user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications() {
        try {
            User user = getAuthenticatedUser();
            System.out.println("📋 Fetching notifications for user ID: " + user.getId());

            List<NotificationDTO> notifications = notificationService.getNotificationsByUser(user.getId());

            System.out.println("✅ Found " + notifications.size() + " notifications");

            return ResponseEntity.ok(ApiResponse.<List<NotificationDTO>>builder()
                    .success(true)
                    .message("Notifications retrieved successfully")
                    .data(notifications != null ? notifications : Collections.emptyList())
                    .build());
        } catch (Exception e) {
            System.err.println("❌ ERROR in getAllNotifications: " + e.getMessage());
            e.printStackTrace();

            // Return empty list instead of error
            return ResponseEntity.ok(ApiResponse.<List<NotificationDTO>>builder()
                    .success(true)
                    .message("No notifications found")
                    .data(Collections.emptyList())
                    .build());
        }
    }

    /**
     * Get unread notifications for logged-in user
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications() {
        try {
            User user = getAuthenticatedUser();
            List<NotificationDTO> notifications = notificationService.getUnreadNotifications(user.getId());

            return ResponseEntity.ok(ApiResponse.<List<NotificationDTO>>builder()
                    .success(true)
                    .message("Unread notifications retrieved successfully")
                    .data(notifications != null ? notifications : Collections.emptyList())
                    .build());
        } catch (Exception e) {
            System.err.println("❌ ERROR in getUnreadNotifications: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.ok(ApiResponse.<List<NotificationDTO>>builder()
                    .success(true)
                    .message("No unread notifications")
                    .data(Collections.emptyList())
                    .build());
        }
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        try {
            User user = getAuthenticatedUser();
            long count = notificationService.getUnreadCount(user.getId());

            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("Unread count retrieved successfully")
                    .data(count)
                    .build());
        } catch (Exception e) {
            System.err.println("❌ ERROR in getUnreadCount: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("No unread notifications")
                    .data(0L)
                    .build());
        }
    }

    /**
     * Mark a specific notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long notificationId) {
        try {
            User user = getAuthenticatedUser();
            notificationService.markAsRead(notificationId, user.getId());

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Notification marked as read")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            System.err.println("❌ ERROR in markAsRead: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error marking notification as read")
                    .build());
        }
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        try {
            User user = getAuthenticatedUser();
            notificationService.markAllAsRead(user.getId());

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("All notifications marked as read")
                    .build());
        } catch (Exception e) {
            System.err.println("❌ ERROR in markAllAsRead: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error marking all as read")
                    .build());
        }
    }

    /**
     * Delete a specific notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long notificationId) {
        try {
            User user = getAuthenticatedUser();
            notificationService.deleteNotification(notificationId, user.getId());

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Notification deleted successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            System.err.println("❌ ERROR in deleteNotification: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error deleting notification")
                    .build());
        }
    }

    /**
     * Helper method to get authenticated user
     */
    private User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                System.err.println("❌ No authentication found in SecurityContext");
                throw new IllegalArgumentException("Not authenticated");
            }

            String username = authentication.getName();
            System.out.println("🔍 Authenticated username: " + username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            System.out.println("✅ Found user with ID: " + user.getId());

            return user;
        } catch (Exception e) {
            System.err.println("❌ ERROR in getAuthenticatedUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}