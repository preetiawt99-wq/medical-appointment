package com.ignou.smartmedical.repository;

import com.ignou.smartmedical.entity.RiskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RiskReportRepository extends JpaRepository<RiskReport, Long> {
    List<RiskReport> findByPatientPatientIdOrderByReportDateDesc(Long patientId);
    List<RiskReport> findAllByOrderByReportDateDesc();
}