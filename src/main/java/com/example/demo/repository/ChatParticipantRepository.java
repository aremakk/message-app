package com.example.demo.repository;

import com.example.demo.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository
        extends JpaRepository<ChatParticipant, Long> {

    List<ChatParticipant> findByUserId(Long userId);
}