package com.tariff.news.article;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleEmbeddingRepo extends JpaRepository<ArticleEmbedding, Long> {

    @Query(value = """
        SELECT * FROM article_embedding
        WHERE embedding IS NOT NULL
        ORDER BY cast(embedding as vector) <-> cast(:embedding as vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ArticleEmbedding> findClosestArticles(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = """
        SELECT cast(cast(embedding as vector) <-> cast(:embedding as vector) as text) as distance
        FROM article_embedding
        WHERE embedding IS NOT NULL
        ORDER BY cast(embedding as vector) <-> cast(:embedding as vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findClosestDistances(@Param("embedding") String embedding, @Param("limit") int limit);

    List<ArticleEmbedding> findByTopic(String topic);

    List<ArticleEmbedding> findByUrl(String url);

    // Fallback Java-side method to retrieve stored embeddings for similarity ranking
    List<ArticleEmbedding> findAllByEmbeddingIsNotNull();

    @Query(value = """
        SELECT * FROM article_embedding
        WHERE cast(embedding as vector) <-> cast(:embedding as vector) < :threshold
        ORDER BY cast(embedding as vector) <-> cast(:embedding as vector)
        """, nativeQuery = true)
    List<ArticleEmbedding> findSimilarArticles(@Param("embedding") String embedding, @Param("threshold") double threshold);
}