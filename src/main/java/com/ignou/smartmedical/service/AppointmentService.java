package com.ignou.smartmedical.service;

// Implements: Appointment Management Module — synopsis section 9.4

import com.ignou.smartmedical.dto.request.BookAppointmentRequest;
import com.ignou.smartmedical.dto.request.RescheduleAppointmentRequest;
import com.ignou.smartmedical.dto.response.AppointmentResponse;
import com.ignou.smartmedical.entity.Appointment;
import com.ignou.smartmedical.entity.Doctor;
import com.ignou.smartmedical.entity.DoctorSlot;
import com.ignou.smartmedical.entity.Patient;
import com.ignou.smartmedical.enums.AppointmentStatus;
import com.ignou.smartmedical.exception.ResourceNotFoundException;
import com.ignou.smartmedical.exception.SlotUnavailableException;
import com.ignou.smartmedical.repository.AppointmentRepository;
import com.ignou.smartmedical.repository.DoctorRepository;
import com.ignou.smartmedical.repository.DoctorSlotRepository;
import com.ignou.smartmedical.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorSlotRepository doctorSlotRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public AppointmentResponse bookAppointment(Long patientId, BookAppointmentRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + req.getDoctorId()));

        DoctorSlot slot = doctorSlotRepository.findById(req.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor slot not found with id: " + req.getSlotId()));

        if (!slot.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new IllegalArgumentException("The selected slot does not belong to the selected doctor");
        }

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new SlotUnavailableException("The selected slot on " + slot.getSlotDate() + " at " + slot.getSlotTime() + " is already booked.");
        }

        // Lock/Mark slot as booked
        slot.setIsBooked(true);
        doctorSlotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSlot(slot);
        appointment.setAppointmentDate(slot.getSlotDate());
        appointment.setAppointmentTime(slot.getSlotTime());
        appointment.setReason(req.getReason());
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);

        // Record audit trail
        auditLogService.logAction(patientId, "BOOK_APPOINTMENT", "APPOINTMENT",
                "Appointment #" + saved.getAppointmentId() + " booked for " + saved.getAppointmentDate() + " " + saved.getAppointmentTime());

        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long currentUserId, boolean isPatient) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        if (isPatient && !app.getPatient().getPatientId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to cancel this appointment");
        }

        app.setStatus(AppointmentStatus.CANCELLED);

        // Free up slot if exists
        if (app.getSlot() != null) {
            DoctorSlot slot = app.getSlot();
            slot.setIsBooked(false);
            doctorSlotRepository.save(slot);
        }

        Appointment updated = appointmentRepository.save(app);

        // Record audit trail
        auditLogService.logAction(currentUserId, "CANCEL_APPOINTMENT", "APPOINTMENT",
                "Appointment #" + appointmentId + " cancelled");

        return mapToResponse(updated);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Long appointmentId, Long currentUserId, boolean isPatient, RescheduleAppointmentRequest req) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        if (isPatient && !app.getPatient().getPatientId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to reschedule this appointment");
        }

        if (app.getStatus() == AppointmentStatus.CANCELLED || app.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reschedule an appointment that is already " + app.getStatus());
        }

        DoctorSlot newSlot = doctorSlotRepository.findById(req.getNewSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor slot not found with id: " + req.getNewSlotId()));

        if (!newSlot.getDoctor().getDoctorId().equals(app.getDoctor().getDoctorId())) {
            throw new IllegalArgumentException("The new slot must belong to the same doctor (" + app.getDoctor().getName() + ")");
        }

        if (Boolean.TRUE.equals(newSlot.getIsBooked())) {
            throw new SlotUnavailableException("The selected slot on " + newSlot.getSlotDate() + " at " + newSlot.getSlotTime() + " is already booked.");
        }

        // Release prior slot
        if (app.getSlot() != null) {
            DoctorSlot oldSlot = app.getSlot();
            oldSlot.setIsBooked(false);
            doctorSlotRepository.save(oldSlot);
        }

        // Reserve new slot
        newSlot.setIsBooked(true);
        doctorSlotRepository.save(newSlot);

        app.setSlot(newSlot);
        app.setAppointmentDate(newSlot.getSlotDate());
        app.setAppointmentTime(newSlot.getSlotTime());
        if (req.getReason() != null && !req.getReason().trim().isEmpty()) {
            app.setReason(req.getReason().trim());
        }
        app.setStatus(AppointmentStatus.PENDING);

        Appointment updated = appointmentRepository.save(app);

        // Record audit trail
        auditLogService.logAction(currentUserId, "RESCHEDULE_APPOINTMENT", "APPOINTMENT",
                "Appointment #" + appointmentId + " rescheduled to " + newSlot.getSlotDate() + " " + newSlot.getSlotTime());

        return mapToResponse(updated);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        return updateStatus(appointmentId, newStatus, null);
    }

    @Transactional
    public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatus newStatus, String doctorNotes) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        app.setStatus(newStatus);
        if (doctorNotes != null && !doctorNotes.trim().isEmpty()) {
            app.setDoctorNotes(doctorNotes.trim());
        }

        if (newStatus == AppointmentStatus.CANCELLED && app.getSlot() != null) {
            DoctorSlot slot = app.getSlot();
            slot.setIsBooked(false);
            doctorSlotRepository.save(slot);
        }

        Appointment updated = appointmentRepository.save(app);

        // Record audit trail
        auditLogService.logAction(null, "UPDATE_STATUS", "APPOINTMENT",
                "Appointment #" + appointmentId + " status updated to " + newStatus);

        return mapToResponse(updated);
    }

    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctorId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public Page<AppointmentResponse> getAllAppointments(AppointmentStatus status, Pageable pageable) {
        Page<Appointment> page;
        if (status != null) {
            page = appointmentRepository.findByStatus(status, pageable);
        } else {
            page = appointmentRepository.findAll(pageable);
        }
        return page.map(this::mapToResponse);
    }

    public AppointmentResponse mapToResponse(Appointment a) {
        AppointmentResponse res = new AppointmentResponse();
        res.setAppointmentId(a.getAppointmentId());
        res.setPatientId(a.getPatient().getPatientId());
        res.setPatientName(a.getPatient().getName());
        res.setPatientMobile(a.getPatient().getMobile());
        res.setPatientBloodGroup(a.getPatient().getBloodGroup());
        res.setPatientMedicalHistory(a.getPatient().getMedicalHistory());
        res.setDoctorId(a.getDoctor().getDoctorId());
        res.setDoctorName(a.getDoctor().getName());
        res.setDoctorSpecialization(a.getDoctor().getSpecialization());
        res.setSlotId(a.getSlot() != null ? a.getSlot().getSlotId() : null);
        res.setAppointmentDate(a.getAppointmentDate());
        res.setAppointmentTime(a.getAppointmentTime());
        res.setReason(a.getReason());
        res.setDoctorNotes(a.getDoctorNotes());
        res.setStatus(a.getStatus());
        res.setCreatedAt(a.getCreatedAt());
        return res;
    }
}