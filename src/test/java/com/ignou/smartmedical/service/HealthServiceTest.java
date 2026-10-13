package com.ignou.smartmedical.service;

import com.ignou.smartmedical.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HealthServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private HealthService healthService;

    @Test
    @DisplayName("Should report the database as up when a query succeeds")
    void testDatabaseUp() {
        when(doctorRepository.count()).thenReturn(5L);

        assertTrue(healthService.isDatabaseUp());
    }

    @Test
    @DisplayName("Should report the database as down instead of failing when it is unreachable")
    void testDatabaseDown() {
        when(doctorRepository.count()).thenThrow(new DataAccessResourceFailureException("Communications link failure"));

        assertFalse(healthService.isDatabaseUp());
    }
}
