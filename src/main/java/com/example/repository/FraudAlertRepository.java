package com.example.repository;

import com.example.model.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByAccountNumber(String accountNumber);
    
    List<FraudAlert> findByStatus(String status);
    
    List<FraudAlert> findBySeverity(String severity);
}

