package com.example.repository;

import com.example.model.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {
    List<ComplianceReport> findByAccountNumber(String accountNumber);
    
    List<ComplianceReport> findByCustomerId(String customerId);
    
    List<ComplianceReport> findByComplianceType(String complianceType);
    
    List<ComplianceReport> findByStatus(String status);
}

