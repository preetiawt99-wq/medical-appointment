package com.ignou.smartmedical.service;

// Supports the keep-alive health check used when hosted on a free cloud plan

import com.ignou.smartmedical.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Runs a real query so that every health check is also database activity,
     * which keeps an inactivity-based free database plan from powering off.
     */
    public boolean isDatabaseUp() {
        try {
            doctorRepository.count();
            return true;
        } catch (RuntimeException ex) {
            log.warn("Health check could not reach the database: {}", ex.getMessage());
            return false;
        }
    }
}
