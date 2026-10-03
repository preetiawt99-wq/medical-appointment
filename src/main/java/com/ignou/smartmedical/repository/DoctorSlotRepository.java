package com.ignou.smartmedical.repository;

import com.ignou.smartmedical.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {
    List<DoctorSlot> findByDoctorDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);
    List<DoctorSlot> findByDoctorDoctorIdAndSlotDateAndIsBookedFalse(Long doctorId, LocalDate slotDate);
    List<DoctorSlot> findByDoctorDoctorIdOrderBySlotDateAscSlotTimeAsc(Long doctorId);
    boolean existsByDoctorDoctorIdAndSlotDateAndSlotTime(Long doctorId, LocalDate slotDate, LocalTime slotTime);
}