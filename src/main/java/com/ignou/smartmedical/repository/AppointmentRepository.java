package com.ignou.smartmedical.repository;

import com.ignou.smartmedical.entity.Appointment;
import com.ignou.smartmedical.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);
    List<Appointment> findByDoctorDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(Long doctorId);
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    long countByStatus(AppointmentStatus status);
}