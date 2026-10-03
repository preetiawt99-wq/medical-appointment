package com.ignou.smartmedical.service;

import com.ignou.smartmedical.dto.request.BookAppointmentRequest;
import com.ignou.smartmedical.dto.request.RescheduleAppointmentRequest;
import com.ignou.smartmedical.dto.response.AppointmentResponse;
import com.ignou.smartmedical.entity.Appointment;
import com.ignou.smartmedical.entity.Doctor;
import com.ignou.smartmedical.entity.DoctorSlot;
import com.ignou.smartmedical.entity.Patient;
import com.ignou.smartmedical.enums.AppointmentStatus;
import com.ignou.smartmedical.exception.SlotUnavailableException;
import com.ignou.smartmedical.repository.AppointmentRepository;
import com.ignou.smartmedical.repository.DoctorRepository;
import com.ignou.smartmedical.repository.DoctorSlotRepository;
import com.ignou.smartmedical.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorSlotRepository doctorSlotRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private DoctorSlot slot;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setPatientId(1L);
        patient.setName("Preeti");
        patient.setMobile("9876543210");

        doctor = new Doctor();
        doctor.setDoctorId(2L);
        doctor.setName("Dr. Rajesh Sharma");
        doctor.setSpecialization("Cardiology");

        slot = new DoctorSlot(doctor, LocalDate.now().plusDays(1), LocalTime.of(10, 0), false);
        slot.setSlotId(10L);
    }

    @Test
    @DisplayName("Should successfully book available slot and set is_booked = true")
    void testBookAppointmentSuccess() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(2L);
        req.setSlotId(10L);
        req.setReason("Chest tightness checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(doctorSlotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(doctorSlotRepository.save(any(DoctorSlot.class))).thenReturn(slot);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> {
            Appointment a = i.getArgument(0);
            a.setAppointmentId(500L);
            return a;
        });

        AppointmentResponse response = appointmentService.bookAppointment(1L, req);

        assertNotNull(response);
        assertEquals(500L, response.getAppointmentId());
        assertEquals(AppointmentStatus.PENDING, response.getStatus());
        assertTrue(slot.getIsBooked(), "Doctor slot must be marked booked");
        verify(doctorSlotRepository, times(1)).save(slot);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(auditLogService, times(1)).logAction(eq(1L), eq("BOOK_APPOINTMENT"), eq("APPOINTMENT"), anyString());
    }

    @Test
    @DisplayName("Should reject booking and throw SlotUnavailableException when slot is already booked")
    void testBookAppointmentAlreadyBookedThrowsException() {
        slot.setIsBooked(true); // already booked!

        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(2L);
        req.setSlotId(10L);
        req.setReason("Second attempt");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(doctorSlotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(SlotUnavailableException.class, () -> {
            appointmentService.bookAppointment(1L, req);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should successfully reschedule appointment releasing previous slot and claiming new slot")
    void testRescheduleAppointmentSuccess() {
        Appointment existingApp = new Appointment();
        existingApp.setAppointmentId(100L);
        existingApp.setPatient(patient);
        existingApp.setDoctor(doctor);
        existingApp.setSlot(slot);
        existingApp.setAppointmentDate(slot.getSlotDate());
        existingApp.setAppointmentTime(slot.getSlotTime());
        existingApp.setStatus(AppointmentStatus.PENDING);
        slot.setIsBooked(true);

        DoctorSlot newSlot = new DoctorSlot(doctor, LocalDate.now().plusDays(2), LocalTime.of(11, 0), false);
        newSlot.setSlotId(20L);

        RescheduleAppointmentRequest req = new RescheduleAppointmentRequest(20L, "Doctor requested morning slot change");

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existingApp));
        when(doctorSlotRepository.findById(20L)).thenReturn(Optional.of(newSlot));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse res = appointmentService.rescheduleAppointment(100L, 1L, true, req);

        assertNotNull(res);
        assertEquals(100L, res.getAppointmentId());
        assertEquals(newSlot.getSlotDate(), res.getAppointmentDate());
        assertEquals(newSlot.getSlotTime(), res.getAppointmentTime());
        assertEquals(AppointmentStatus.PENDING, res.getStatus());
        assertFalse(slot.getIsBooked(), "Prior slot must be released");
        assertTrue(newSlot.getIsBooked(), "New slot must be marked booked");
        verify(doctorSlotRepository, times(1)).save(slot);
        verify(doctorSlotRepository, times(1)).save(newSlot);
        verify(auditLogService, times(1)).logAction(eq(1L), eq("RESCHEDULE_APPOINTMENT"), eq("APPOINTMENT"), anyString());
    }

    @Test
    @DisplayName("Should reject reschedule when the target new slot is already booked")
    void testRescheduleAppointmentTargetSlotConflict() {
        Appointment existingApp = new Appointment();
        existingApp.setAppointmentId(100L);
        existingApp.setPatient(patient);
        existingApp.setDoctor(doctor);
        existingApp.setSlot(slot);
        existingApp.setStatus(AppointmentStatus.PENDING);

        DoctorSlot busySlot = new DoctorSlot(doctor, LocalDate.now().plusDays(2), LocalTime.of(11, 0), true);
        busySlot.setSlotId(30L);

        RescheduleAppointmentRequest req = new RescheduleAppointmentRequest(30L, "Conflicting slot test");

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existingApp));
        when(doctorSlotRepository.findById(30L)).thenReturn(Optional.of(busySlot));

        assertThrows(SlotUnavailableException.class, () -> {
            appointmentService.rescheduleAppointment(100L, 1L, true, req);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should update status to COMPLETED with doctor prescription notes")
    void testUpdateStatusWithDoctorNotes() {
        Appointment existingApp = new Appointment();
        existingApp.setAppointmentId(105L);
        existingApp.setPatient(patient);
        existingApp.setDoctor(doctor);
        existingApp.setSlot(slot);
        existingApp.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(105L)).thenReturn(Optional.of(existingApp));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        AppointmentResponse res = appointmentService.updateStatus(105L, AppointmentStatus.COMPLETED, "Prescribed Tab Paracetamol 650mg TDS for 3 days.");

        assertNotNull(res);
        assertEquals(AppointmentStatus.COMPLETED, res.getStatus());
        assertEquals("Prescribed Tab Paracetamol 650mg TDS for 3 days.", res.getDoctorNotes());
        verify(appointmentRepository, times(1)).save(existingApp);
    }
}