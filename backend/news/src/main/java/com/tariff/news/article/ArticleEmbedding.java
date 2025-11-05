package com.tariff.news.article;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(columnDefinition = "vector(1536)")
    @ColumnTransformer(read = "embedding::text", write = "cast(? as vector(1536))")
    @JdbcTypeCode(SqlTypes.VARCHAR) // Ensure VARCHAR binding for PostgreSQL
    @Convert(converter = com.tariff.news.article.FloatArrayConverter.class)
    private float[] embedding; // Stored as Postgres vector(1536)

    private String topic; // The extracted topic used to find this article
    
    @Column(columnDefinition = "TEXT")
    private String queryContext; // GPT-produced 1-2 sentence context tying article to a query

    private String lastSeenQuery; // raw query that produced/updated this record

    @Column(columnDefinition = "TEXT")
    private String queryEmbedding; // optional: embedding of query+article (string)

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