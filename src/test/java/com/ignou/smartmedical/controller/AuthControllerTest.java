package com.ignou.smartmedical.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ignou.smartmedical.dto.request.LoginRequest;
import com.ignou.smartmedical.dto.response.JwtResponse;
import com.ignou.smartmedical.enums.Role;
import com.ignou.smartmedical.service.AuthService;
import com.ignou.smartmedical.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("TC-01: Valid Login should return JWT token and HTTP 200 OK")
    void testTC01_LoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("admin");
        req.setPassword("Admin@123");
        req.setRole(Role.ADMIN);

        JwtResponse mockJwt = new JwtResponse("mock-jwt-token", 1L, "admin", Role.ADMIN, "Administrator");
        when(authService.login(any(LoginRequest.class))).thenReturn(mockJwt);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("TC-03: Invalid Login should return HTTP 401 Unauthorized with Error Message")
    void testTC03_InvalidLogin_ErrorMessage() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("admin");
        req.setPassword("WrongPassword");
        req.setRole(Role.ADMIN);

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid Admin username/email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Admin username/email or password"));
    }

    @Test
    @DisplayName("TC-05: Unauthorized Access (Patient attempting Admin endpoint) should return HTTP 403 Forbidden")
    void testTC05_UnauthorizedAccess_Forbidden() throws Exception {
        String patientToken = "valid.patient.jwt.token";
        when(jwtUtil.isTokenValid(patientToken)).thenReturn(true);
        when(jwtUtil.extractUsername(patientToken)).thenReturn("preeti@example.com");
        when(jwtUtil.extractRole(patientToken)).thenReturn("PATIENT");
        when(jwtUtil.extractId(patientToken)).thenReturn(1L);

        mockMvc.perform(get("/api/admin/reports/summary")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}