package com.tariff.news.history;

import java.time.OffsetDateTime;
import java.util.List;

public class ConversationDto {
    private Long id;
    private String title;
    private List<MessageDto> messages;
    private OffsetDateTime createdAt;

    public ConversationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<MessageDto> getMessages() { return messages; }
    public void setMessages(List<MessageDto> messages) { this.messages = messages; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}