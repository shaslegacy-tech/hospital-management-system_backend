package com.hospital.hms.controller;

import com.hospital.hms.dto.response.NotificationResponseDTO;
import com.hospital.hms.model.Notification;
import com.hospital.hms.model.User;
import com.hospital.hms.repository.NotificationRepository;
import com.hospital.hms.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Notifications")
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    private NotificationResponseDTO toDTO(Notification n) {
        return new NotificationResponseDTO(
                n.getId(),
                n.getMessage(),
                n.getType(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt() != null ? n.getCreatedAt().toString() : null
        );
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
            Authentication authentication) {
        User user = currentUser(authentication);
        List<NotificationResponseDTO> result = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .limit(30)
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication) {
        User user = currentUser(authentication);
        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found!"));
        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok("Marked as read");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllRead(Authentication authentication) {
        User user = currentUser(authentication);
        List<Notification> unread = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok("All marked as read");
    }
}