package com.tariff.news.article;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Test-only entity to shadow the production ArticleEmbedding mapping.
 * Uses H2-friendly column types and removes Postgres-specific vector/transformer annotations.
 */
@Entity
@Table(name = "article_embedding")
public class ArticleEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT", unique = true)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String cleanedText;

    // Store as TEXT via converter in H2; no pgvector or ColumnTransformer here
    @Column(columnDefinition = "TEXT")
    @Convert(converter = com.tariff.news.article.FloatArrayConverter.class)
    private float[] embedding;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String queryContext;

    private String lastSeenQuery;

    @Column(columnDefinition = "TEXT")
    private String queryEmbedding;

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getCleanedText() { return cleanedText; }
    public float[] getEmbedding() { return embedding; }
    public String getTopic() { return topic; }
    public String getQueryContext() { return queryContext; }
    public String getLastSeenQuery() { return lastSeenQuery; }
    public String getQueryEmbedding() { return queryEmbedding; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setUrl(String url) { this.url = url; }
    public void setCleanedText(String cleanedText) { this.cleanedText = cleanedText; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setQueryContext(String queryContext) { this.queryContext = queryContext; }
    public void setLastSeenQuery(String lastSeenQuery) { this.lastSeenQuery = lastSeenQuery; }
    public void setQueryEmbedding(String queryEmbedding) { this.queryEmbedding = queryEmbedding; }
}
