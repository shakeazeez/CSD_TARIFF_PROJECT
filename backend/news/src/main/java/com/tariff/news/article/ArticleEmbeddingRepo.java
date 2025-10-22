package com.tariff.news.article;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ArticleEmbeddingRepo extends JpaRepository<ArticleEmbedding, Long> {

    @Query(value = """
        SELECT * FROM article_embedding
        WHERE embedding IS NOT NULL
        ORDER BY embedding <-> cast(:embedding as vector(1536))
        LIMIT :limit
        """, nativeQuery = true)
    List<ArticleEmbedding> findClosestArticles(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = """
        SELECT cast(embedding <-> cast(:embedding as vector(1536)) as text)
        FROM article_embedding
        WHERE embedding IS NOT NULL
        ORDER BY embedding <-> cast(:embedding as vector(1536))
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findClosestDistances(@Param("embedding") String embedding, @Param("limit") int limit);

    List<ArticleEmbedding> findByTopic(String topic);

    List<ArticleEmbedding> findByUrl(String url);

    // Fallback Java-side method to retrieve stored embeddings for similarity ranking
    List<ArticleEmbedding> findAllByEmbeddingIsNotNull();

    @Query(value = """
        SELECT * FROM article_embedding
        WHERE embedding <-> cast(:embedding as vector(1536)) < :threshold
        ORDER BY embedding <-> cast(:embedding as vector(1536))
        """, nativeQuery = true)
    List<ArticleEmbedding> findSimilarArticles(@Param("embedding") String embedding, @Param("threshold") double threshold);

}