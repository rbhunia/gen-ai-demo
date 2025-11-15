package com.example.repository;

import com.example.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    Optional<RiskAssessment> findFirstByAccountNumberOrderByAssessmentDateDesc(String accountNumber);
    
    List<RiskAssessment> findByAccountNumber(String accountNumber);
    
    List<RiskAssessment> findByCustomerId(String customerId);
}

