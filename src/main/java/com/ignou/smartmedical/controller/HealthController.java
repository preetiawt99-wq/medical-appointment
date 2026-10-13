package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    // Public endpoint for uptime monitors and the keep-alive ping
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        if (healthService.isDatabaseUp()) {
            return ResponseEntity.ok(Map.of("status", "UP", "database", "UP"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "DOWN", "database", "DOWN"));
    }
}
