package com.example.repository;

import com.example.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByCustomerIdOrderByTimestampDesc(String customerId);
    void deleteByCustomerId(String customerId);
}

