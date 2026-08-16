package com.hospital.hms.service;
 
import com.hospital.hms.model.AuditLog;
import com.hospital.hms.model.User;
import com.hospital.hms.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
@Slf4j
@Service
public class AuditLogService {
 
    @Autowired
    private AuditLogRepository auditLogRepository;
 
    public void log(User user, String action, String entityType, Long entityId, String details) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUser(user);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setDetails(details);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }
}