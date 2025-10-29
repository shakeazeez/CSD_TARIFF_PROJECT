package com.tariff.news.history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatHistoryRepo extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUsernameOrderByCreatedAtDesc(String username);
    List<ChatHistory> findByUsernameAndTopicOrderByCreatedAtDesc(String username, String topic);
}
