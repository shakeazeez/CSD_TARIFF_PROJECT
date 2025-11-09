package com.tariff.news.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepo repo;
    private final ObjectMapper objectMapper;

    public ChatHistory save(String username, String topic, String queryText, String synthesizedAnswer, Object articles, Long conversationId) {
        try {
            ChatHistory h;
            if (conversationId != null) {
                Optional<ChatHistory> opt = repo.findById(conversationId);
                if (opt.isPresent()) {
                    h = opt.get();
                    // parse messages
                    List<Map<String, Object>> msgs;
                    if (h.getMessages() == null || h.getMessages().trim().isEmpty()) {
                        msgs = new ArrayList<>();
                    } else {
                        msgs = objectMapper.readValue(h.getMessages(), new TypeReference<List<Map<String, Object>>>(){});
                    }
                    Map<String, Object> newMsg = new HashMap<>();
                    newMsg.put("query", queryText);
                    newMsg.put("response", synthesizedAnswer);
                    newMsg.put("sources", articles);
                    msgs.add(newMsg);
                    h.setMessages(objectMapper.writeValueAsString(msgs));
                } else {
                    // Conversation not found, create new
                    h = new ChatHistory();
                    h.setUsername(username);
                    h.setTopic(topic);
                    List<Map<String, Object>> msgs = new ArrayList<>();
                    Map<String, Object> newMsg = new HashMap<>();
                    newMsg.put("query", queryText);
                    newMsg.put("response", synthesizedAnswer);
                    newMsg.put("sources", articles);
                    msgs.add(newMsg);
                    h.setMessages(objectMapper.writeValueAsString(msgs));
                }
            } else {
                h = new ChatHistory();
                h.setUsername(username);
                h.setTopic(topic);
                List<Map<String, Object>> msgs = new ArrayList<>();
                Map<String, Object> newMsg = new HashMap<>();
                newMsg.put("query", queryText);
                newMsg.put("response", synthesizedAnswer);
                newMsg.put("sources", articles);
                msgs.add(newMsg);
                h.setMessages(objectMapper.writeValueAsString(msgs));
            }
            return repo.save(h);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chat history", e);
        }
    }

    public List<ChatHistory> findByUser(String username) {
        return repo.findByUsernameOrderByCreatedAtDesc(username);
    }

    public List<ChatHistory> findByUserAndTopic(String username, String topic) {
        return repo.findByUsernameAndTopicOrderByCreatedAtDesc(username, topic);
    }

    public void deleteById(Long id) {
        var opt = repo.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Chat history not found");
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            // If already deleted by another transaction, ignore
            if (repo.findById(id).isEmpty()) {
                return;
            }
            throw e;
        }
    }

    public Optional<ChatHistory> findByIdAndUsername(Long id, String username) {
        var opt = repo.findById(id);
        if (opt.isPresent() && opt.get().getUsername().equals(username)) {
            return opt;
        }
        return Optional.empty();
    }

    public Optional<ChatHistory> findById(Long id) {
        return repo.findById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }
}
