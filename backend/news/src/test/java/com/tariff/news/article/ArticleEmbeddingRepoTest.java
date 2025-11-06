package com.tariff.news.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Unit tests for ArticleEmbeddingRepo
 * These tests focus on repository behavior without requiring a real database
 */
@ExtendWith(SpringExtension.class)
public class ArticleEmbeddingRepoTest {

    @MockBean
    private ArticleEmbeddingRepo articleEmbeddingRepo;

    @Test
    void testFindByTopic_ReturnsMatchingArticles() {
        // Arrange
        String topic = "trade policy";
        List<ArticleEmbedding> expectedArticles = createMockArticles();
        
        when(articleEmbeddingRepo.findByTopic(topic)).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = articleEmbeddingRepo.findByTopic(topic);

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findByTopic(topic);
    }

    @Test
    void testFindByUrl_ReturnsMatchingArticle() {
        // Arrange
        String url = "https://example.com/article1";
        List<ArticleEmbedding> expectedArticles = createMockArticles().subList(0, 1);
        
        when(articleEmbeddingRepo.findByUrl(url)).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = articleEmbeddingRepo.findByUrl(url);

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findByUrl(url);
    }

    @Test
    void testFindAllByEmbeddingIsNotNull_ReturnsArticlesWithEmbeddings() {
        // Arrange
        List<ArticleEmbedding> expectedArticles = createMockArticles();
        
        when(articleEmbeddingRepo.findAllByEmbeddingIsNotNull()).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = articleEmbeddingRepo.findAllByEmbeddingIsNotNull();

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findAllByEmbeddingIsNotNull();
    }

    @Test
    void testFindClosestArticles_ReturnsOrderedResults() {
        // Arrange
        String embedding = "[0.1,0.2,0.3]";
        int limit = 5;
        List<ArticleEmbedding> expectedArticles = createMockArticles();
        
        when(articleEmbeddingRepo.findClosestArticles(embedding, limit)).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = articleEmbeddingRepo.findClosestArticles(embedding, limit);

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findClosestArticles(embedding, limit);
    }

    @Test
    void testFindClosestDistances_ReturnsDistanceValues() {
        // Arrange
        String embedding = "[0.1,0.2,0.3]";
        int limit = 3;
        List<String> expectedDistances = Arrays.asList("0.1", "0.2", "0.3");
        
        when(articleEmbeddingRepo.findClosestDistances(embedding, limit)).thenReturn(expectedDistances);

        // Act
        List<String> result = articleEmbeddingRepo.findClosestDistances(embedding, limit);

        // Assert
        assertEquals(expectedDistances, result);
        verify(articleEmbeddingRepo).findClosestDistances(embedding, limit);
    }

    @Test
    void testFindSimilarArticles_WithThreshold() {
        // Arrange
        String embedding = "[0.1,0.2,0.3]";
        double threshold = 0.8;
        List<ArticleEmbedding> expectedArticles = createMockArticles();
        
        when(articleEmbeddingRepo.findSimilarArticles(embedding, threshold)).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = articleEmbeddingRepo.findSimilarArticles(embedding, threshold);

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findSimilarArticles(embedding, threshold);
    }

    @Test
    void testSave_PersistsArticle() {
        // Arrange
        ArticleEmbedding article = createSingleMockArticle();
        
        when(articleEmbeddingRepo.save(article)).thenReturn(article);

        // Act
        ArticleEmbedding result = articleEmbeddingRepo.save(article);

        // Assert
        assertEquals(article, result);
        verify(articleEmbeddingRepo).save(article);
    }

    @Test
    void testFindAll_ReturnsAllArticles() {
        // Arrange
        List<ArticleEmbedding> expectedArticles = createMockArticles();
        
        when(articleEmbeddingRepo.findAll()).thenReturn(expectedArticles);

        // Act
        List<ArticleEmbedding> result = articleEmbeddingRepo.findAll();

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleEmbeddingRepo).findAll();
    }

    @Test
    void testFindById_ReturnsOptionalArticle() {
        // Arrange
        Long id = 1L;
        ArticleEmbedding article = createSingleMockArticle();
        
        when(articleEmbeddingRepo.findById(id)).thenReturn(Optional.of(article));

        // Act
        Optional<ArticleEmbedding> result = articleEmbeddingRepo.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(article, result.get());
        verify(articleEmbeddingRepo).findById(id);
    }

    @Test
    void testDeleteById_RemovesArticle() {
        // Arrange
        Long id = 1L;
        
        doNothing().when(articleEmbeddingRepo).deleteById(id);

        // Act
        articleEmbeddingRepo.deleteById(id);

        // Assert
        verify(articleEmbeddingRepo).deleteById(id);
    }

    @Test
    void testExistsById_ChecksExistence() {
        // Arrange
        Long id = 1L;
        
        when(articleEmbeddingRepo.existsById(id)).thenReturn(true);

        // Act
        boolean result = articleEmbeddingRepo.existsById(id);

        // Assert
        assertTrue(result);
        verify(articleEmbeddingRepo).existsById(id);
    }

    // Helper methods

    private List<ArticleEmbedding> createMockArticles() {
        List<ArticleEmbedding> articles = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            ArticleEmbedding article = new ArticleEmbedding();
            article.setId((long) i);
            article.setTitle("Trade Article " + i);
            article.setUrl("https://example.com/article" + i);
            article.setCleanedText("Content for article " + i);
            article.setEmbedding(createMockEmbedding());
            article.setTopic("trade");
            article.setQueryContext("Context for article " + i);
            article.setLastSeenQuery("trade policy");
            articles.add(article);
        }
        
        return articles;
    }

    private ArticleEmbedding createSingleMockArticle() {
        ArticleEmbedding article = new ArticleEmbedding();
        article.setId(1L);
        article.setTitle("Sample Trade Article");
        article.setUrl("https://example.com/sample-article");
        article.setCleanedText("Sample content about trade policies");
        article.setEmbedding(createMockEmbedding());
        article.setTopic("trade");
        article.setQueryContext("Sample context");
        article.setLastSeenQuery("trade policy");
        return article;
    }

    private float[] createMockEmbedding() {
        float[] embedding = new float[1536];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = (float) (Math.random() * 2 - 1);
        }
        return embedding;
    }
}
