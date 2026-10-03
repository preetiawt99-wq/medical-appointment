package com.ignou.smartmedical.service;

// Implements: Doctor Management Module — synopsis section 9.3

import com.ignou.smartmedical.dto.request.CreateDoctorRequest;
import com.ignou.smartmedical.dto.request.SlotRequest;
import com.ignou.smartmedical.dto.request.UpdateDoctorRequest;
import com.ignou.smartmedical.dto.response.DoctorResponse;
import com.ignou.smartmedical.dto.response.DoctorSlotResponse;
import com.ignou.smartmedical.entity.Doctor;
import com.ignou.smartmedical.entity.DoctorSlot;
import com.ignou.smartmedical.exception.ResourceNotFoundException;
import com.ignou.smartmedical.repository.DoctorRepository;
import com.ignou.smartmedical.repository.DoctorSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorSlotRepository doctorSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<DoctorResponse> getAllDoctors(String specialization) {
        List<Doctor> doctors;
        if (specialization != null && !specialization.trim().isEmpty()) {
            doctors = doctorRepository.findBySpecializationContainingIgnoreCase(specialization.trim());
        } else {
            doctors = doctorRepository.findAll();
        }
        return doctors.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doc = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        return mapToResponse(doc);
    }

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest req) {
        if (doctorRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Doctor email already exists: " + req.getEmail());
        }

        Doctor doc = new Doctor();
        doc.setName(req.getName());
        doc.setSpecialization(req.getSpecialization());
        doc.setEmail(req.getEmail());
        doc.setPhone(req.getPhone());
        doc.setPassword(passwordEncoder.encode(req.getPassword()));
        doc.setAvailability(req.getAvailability() != null ? req.getAvailability() : "Mon-Fri 09:00 - 17:00");

        Doctor saved = doctorRepository.save(doc);
        return mapToResponse(saved);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long doctorId, UpdateDoctorRequest req) {
        Doctor doc = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        doc.setName(req.getName());
        doc.setSpecialization(req.getSpecialization());
        doc.setPhone(req.getPhone());
        doc.setAvailability(req.getAvailability());

        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            doc.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        Doctor saved = doctorRepository.save(doc);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDoctor(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id: " + doctorId);
        }
        doctorRepository.deleteById(doctorId);
    }

    @Transactional
    public DoctorSlotResponse addSlot(Long doctorId, SlotRequest req) {
        Doctor doc = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (doctorSlotRepository.existsByDoctorDoctorIdAndSlotDateAndSlotTime(doctorId, req.getSlotDate(), req.getSlotTime())) {
            throw new IllegalArgumentException("Slot already exists for this doctor on " + req.getSlotDate() + " at " + req.getSlotTime());
        }

        DoctorSlot slot = new DoctorSlot(doc, req.getSlotDate(), req.getSlotTime(), false);
        DoctorSlot saved = doctorSlotRepository.save(slot);

        return mapSlotToResponse(saved);
    }

    public List<DoctorSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        List<DoctorSlot> slots;
        if (date != null) {
            slots = doctorSlotRepository.findByDoctorDoctorIdAndSlotDateAndIsBookedFalse(doctorId, date);
        } else {
            slots = doctorSlotRepository.findByDoctorDoctorIdOrderBySlotDateAscSlotTimeAsc(doctorId).stream()
                    .filter(s -> !s.getIsBooked())
                    .collect(Collectors.toList());
        }
        return slots.stream().map(this::mapSlotToResponse).collect(Collectors.toList());
    }

    public List<DoctorSlotResponse> getAllSlotsForDoctor(Long doctorId) {
        return doctorSlotRepository.findByDoctorDoctorIdOrderBySlotDateAscSlotTimeAsc(doctorId).stream()
                .map(this::mapSlotToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSlot(Long doctorId, Long slotId) {
        DoctorSlot slot = doctorSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));

        if (!slot.getDoctor().getDoctorId().equals(doctorId)) {
            throw new AccessDeniedException("You can only delete your own slots");
        }

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new IllegalStateException("Cannot delete a booked slot. Cancel the appointment first.");
        }

        doctorSlotRepository.delete(slot);
    }

    public DoctorResponse mapToResponse(Doctor doc) {
        return new DoctorResponse(
                doc.getDoctorId(),
                doc.getName(),
                doc.getSpecialization(),
                doc.getEmail(),
                doc.getPhone(),
                doc.getAvailability(),
                doc.getCreatedAt()
        );
    }

    public DoctorSlotResponse mapSlotToResponse(DoctorSlot slot) {
        return new DoctorSlotResponse(
                slot.getSlotId(),
                slot.getDoctor().getDoctorId(),
                slot.getDoctor().getName(),
                slot.getDoctor().getSpecialization(),
                slot.getSlotDate(),
                slot.getSlotTime(),
                slot.getIsBooked()
        );
    }
}