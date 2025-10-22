package com.tariff.news.article;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "article_embedding")
@Data
public class ArticleEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT", unique = true)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String cleanedText;

    @Column(columnDefinition = "TEXT")
    private String embedding; // Stored as string format "[0.123,-0.456,0.789,...]"

    private String topic; // The extracted topic used to find this article
    
    @Column(columnDefinition = "TEXT")
    private String queryContext; // GPT-produced 1-2 sentence context tying article to a query

    private String lastSeenQuery; // raw query that produced/updated this record

    @Column(columnDefinition = "TEXT")
    private String queryEmbedding; // optional: embedding of query+article (string)
}