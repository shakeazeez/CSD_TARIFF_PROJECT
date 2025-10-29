package com.tariff.news.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    private String synthesizedAnswer; // final GPT answer for the user
    private String source; // "db" or "api"
    private List<ArticleEmbedding> articles; // detailed articles returned
    private Long conversationId; // ID of the conversation for threading

    public String getSynthesizedAnswer() {
        return synthesizedAnswer;
    }

    public void setSynthesizedAnswer(String synthesizedAnswer) {
        this.synthesizedAnswer = synthesizedAnswer;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<ArticleEmbedding> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleEmbedding> articles) {
        this.articles = articles;
    }
}
