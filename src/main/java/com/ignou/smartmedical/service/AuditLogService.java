package com.ignou.smartmedical.service;

// Implements: Audit Component — synopsis section 5.3, 7.3 & 12.5

import com.ignou.smartmedical.entity.AuditLog;
import com.ignou.smartmedical.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog logAction(Long userId, String action, String entity, String details) {
        AuditLog auditLog = new AuditLog(userId, action, entity, details);
        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}