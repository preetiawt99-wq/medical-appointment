package com.ignou.smartmedical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignou.smartmedical.dto.request.AppointmentStatusUpdateRequest;
import com.ignou.smartmedical.dto.request.BookAppointmentRequest;
import com.ignou.smartmedical.dto.request.RescheduleAppointmentRequest;
import com.ignou.smartmedical.dto.response.AppointmentResponse;
import com.ignou.smartmedical.enums.AppointmentStatus;
import com.ignou.smartmedical.service.AppointmentService;
import com.ignou.smartmedical.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MOCK_TOKEN = "valid.mock.jwt.token";

    @BeforeEach
    void setupSecurityMock() {
        when(jwtUtil.isTokenValid(MOCK_TOKEN)).thenReturn(true);
        when(jwtUtil.extractUsername(MOCK_TOKEN)).thenReturn("preeti@example.com");
        when(jwtUtil.extractRole(MOCK_TOKEN)).thenReturn("PATIENT");
        when(jwtUtil.extractId(MOCK_TOKEN)).thenReturn(1L);
    }

    @Test
    @DisplayName("TC-02: Book Appointment with Valid Details should return 201 Created")
    void testBookAppointmentSuccess() throws Exception {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(2L);
        req.setSlotId(10L);
        req.setReason("Chest tightness consultation");

        AppointmentResponse mockResponse = new AppointmentResponse();
        mockResponse.setAppointmentId(201L);
        mockResponse.setPatientId(1L);
        mockResponse.setPatientName("Preeti");
        mockResponse.setDoctorId(2L);
        mockResponse.setDoctorName("Dr. Rajesh Sharma");
        mockResponse.setAppointmentDate(LocalDate.now().plusDays(1));
        mockResponse.setAppointmentTime(LocalTime.of(10, 0));
        mockResponse.setStatus(AppointmentStatus.PENDING);

        when(appointmentService.bookAppointment(eq(1L), any(BookAppointmentRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + MOCK_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(201))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Rajesh Sharma"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/cancel should cancel appointment and return 200 OK")
    void testCancelAppointmentSuccess() throws Exception {
        AppointmentResponse mockCancelled = new AppointmentResponse();
        mockCancelled.setAppointmentId(201L);
        mockCancelled.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentService.cancelAppointment(eq(201L), eq(1L), eq(true)))
                .thenReturn(mockCancelled);

        mockMvc.perform(put("/api/appointments/201/cancel")
                        .header("Authorization", "Bearer " + MOCK_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(201))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/status should update status to CONFIRMED for Doctor/Admin")
    void testUpdateStatusSuccess() throws Exception {
        when(jwtUtil.extractRole(MOCK_TOKEN)).thenReturn("DOCTOR");
        when(jwtUtil.extractId(MOCK_TOKEN)).thenReturn(2L);

        AppointmentStatusUpdateRequest req = new AppointmentStatusUpdateRequest();
        req.setStatus(AppointmentStatus.CONFIRMED);

        AppointmentResponse mockUpdated = new AppointmentResponse();
        mockUpdated.setAppointmentId(201L);
        mockUpdated.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentService.updateStatus(eq(201L), eq(AppointmentStatus.CONFIRMED), any()))
                .thenReturn(mockUpdated);

        mockMvc.perform(put("/api/appointments/201/status")
                        .header("Authorization", "Bearer " + MOCK_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/reschedule should reschedule appointment and return 200 OK")
    void testRescheduleAppointmentSuccess() throws Exception {
        RescheduleAppointmentRequest req = new RescheduleAppointmentRequest(15L, "Urgent morning reschedule");

        AppointmentResponse mockRescheduled = new AppointmentResponse();
        mockRescheduled.setAppointmentId(201L);
        mockRescheduled.setSlotId(15L);
        mockRescheduled.setAppointmentDate(LocalDate.now().plusDays(2));
        mockRescheduled.setAppointmentTime(LocalTime.of(11, 30));
        mockRescheduled.setStatus(AppointmentStatus.PENDING);

        when(appointmentService.rescheduleAppointment(eq(201L), eq(1L), eq(true), any(RescheduleAppointmentRequest.class)))
                .thenReturn(mockRescheduled);

        mockMvc.perform(put("/api/appointments/201/reschedule")
                        .header("Authorization", "Bearer " + MOCK_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(201))
                .andExpect(jsonPath("$.slotId").value(15))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}