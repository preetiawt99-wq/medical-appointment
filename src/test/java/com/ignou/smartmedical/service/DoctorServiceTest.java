package com.ignou.smartmedical.service;

import com.ignou.smartmedical.dto.request.SlotRequest;
import com.ignou.smartmedical.dto.response.DoctorSlotResponse;
import com.ignou.smartmedical.entity.Doctor;
import com.ignou.smartmedical.entity.DoctorSlot;
import com.ignou.smartmedical.exception.ResourceNotFoundException;
import com.ignou.smartmedical.repository.DoctorRepository;
import com.ignou.smartmedical.repository.DoctorSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorSlotRepository doctorSlotRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private DoctorSlot slot;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setDoctorId(1L);
        doctor.setName("Dr. Rohit Sharma");
        doctor.setSpecialization("Cardiology");
        doctor.setEmail("sharma@smartmed.com");

        slot = new DoctorSlot();
        slot.setSlotId(10L);
        slot.setDoctor(doctor);
        slot.setSlotDate(LocalDate.now().plusDays(2));
        slot.setSlotTime(LocalTime.of(10, 0));
        slot.setIsBooked(false);
    }

    @Test
    @DisplayName("Should delete unbooked slot owned by doctor successfully")
    void testDeleteSlot_Success() {
        when(doctorSlotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertDoesNotThrow(() -> doctorService.deleteSlot(1L, 10L));
        verify(doctorSlotRepository, times(1)).delete(slot);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when doctor attempts to delete another doctor's slot")
    void testDeleteSlot_AccessDenied() {
        when(doctorSlotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(AccessDeniedException.class, () -> doctorService.deleteSlot(2L, 10L));
        verify(doctorSlotRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when attempting to delete a booked slot")
    void testDeleteSlot_BookedSlotConflict() {
        slot.setIsBooked(true);
        when(doctorSlotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(IllegalStateException.class, () -> doctorService.deleteSlot(1L, 10L));
        verify(doctorSlotRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when slot does not exist")
    void testDeleteSlot_NotFound() {
        when(doctorSlotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> doctorService.deleteSlot(1L, 99L));
        verify(doctorSlotRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should create slot successfully when no conflict exists")
    void testAddSlot_Success() {
        SlotRequest req = new SlotRequest(LocalDate.now().plusDays(3), LocalTime.of(14, 0));

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorSlotRepository.existsByDoctorDoctorIdAndSlotDateAndSlotTime(1L, req.getSlotDate(), req.getSlotTime())).thenReturn(false);
        when(doctorSlotRepository.save(any(DoctorSlot.class))).thenAnswer(invocation -> {
            DoctorSlot saved = invocation.getArgument(0);
            saved.setSlotId(20L);
            return saved;
        });

        DoctorSlotResponse res = doctorService.addSlot(1L, req);

        assertNotNull(res);
        assertEquals(20L, res.getSlotId());
        assertEquals(req.getSlotDate(), res.getSlotDate());
        verify(doctorSlotRepository, times(1)).save(any(DoctorSlot.class));
    }
}
