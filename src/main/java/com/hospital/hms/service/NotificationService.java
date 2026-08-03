package com.hospital.hms.service;

import com.hospital.hms.model.Notification;
import com.hospital.hms.model.User;
import com.hospital.hms.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void notify(User user, String type, String message, String link) {
        try {
            Notification n = new Notification();
            n.setUser(user);
            n.setType(type);
            n.setMessage(message);
            n.setLink(link);
            n.setRead(false);
            notificationRepository.save(n);
        } catch (Exception e) {
            log.error("Failed to create notification: {}", e.getMessage());
        }
    }
}