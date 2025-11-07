package com.tariff.news.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tariff.news.article.ArticleEmbedding;
import com.tariff.news.article.ArticleEmbeddingRepo;
import com.tariff.news.dto.NewsResponse;
import com.tariff.news.service.NewsEmbeddingService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test-only configuration that provides a deterministic NewsEmbeddingService for integration tests.
 * - Subclasses NewsEmbeddingService and overrides network-dependent methods to avoid real HTTP calls.
 * - Delegates reads to the JPA repository and uses simple cosine similarity for stable ranking.
 * - Enables REST Assured controller tests to exercise endpoints end-to-end without external dependencies.
 */
@TestConfiguration
public class NewsTestServiceConfig {

    @Bean
    @Primary
    public NewsEmbeddingService testNewsEmbeddingService(ArticleEmbeddingRepo repo) {
        // Provide a fake NewsEmbeddingService that avoids network calls and delegates reads to the repo
        return new NewsEmbeddingService(WebClient.create(), new ObjectMapper(), repo) {
            @Override
            public NewsResponse processQuery(String query) {
                if (query != null && query.contains("ARTICLES=ONE")) {
                    com.tariff.news.dto.ArticleEmbedding art = new com.tariff.news.dto.ArticleEmbedding();
                    art.setTitle("A1");
                    art.setUrl("https://ex/a1");
                    return new NewsResponse("base", "db", List.of(art), null);
                }
                if (query != null && query.contains("ARTICLES=NULL")) {
                    return new NewsResponse("base", "db", null, null);
                }
                if (query != null && query.contains("THROW")) {
                    throw new RuntimeException("fail");
                }
                // default: empty list to trigger controller fallback
                return new NewsResponse("base", "db", List.of(), null);
            }

            @Override
            public String extractTopic(String query) { return "test-topic"; }

            @Override
            public List<ArticleEmbedding> findSimilarArticles(String embedding, int limit) {
                double[] query = parseEmbedding(embedding);
                return repo.findAllByEmbeddingIsNotNull().stream()
                    .sorted(Comparator.comparingDouble((ArticleEmbedding e) -> -cosine(query, toDouble(e.getEmbedding()))))
                    .limit(limit)
                    .collect(Collectors.toList());
            }

            @Override
            public List<ArticleEmbedding> findArticlesByTopic(String topic) {
                return repo.findByTopic(topic);
            }

            @Override
            public List<ArticleEmbedding> getAllArticles() {
                return repo.findAll();
            }

            @Override
            public List<ArticleEmbedding> findArticlesSimilarToQuery(String query, int limit) {
                // Deterministic, network-free similarity: use a fixed query vector so the
                // seeded trade article (0.1,0.2,0.3) ranks first.
                double[] q = new double[]{0.1, 0.2, 0.3};
                return repo.findAllByEmbeddingIsNotNull().stream()
                    .sorted(Comparator.comparingDouble((ArticleEmbedding e) -> -cosine(q, toDouble(e.getEmbedding()))))
                    .limit(limit)
                    .collect(Collectors.toList());
            }

            private double[] parseEmbedding(String str) {
                String s = str.trim();
                if (s.startsWith("[") && s.endsWith("]")) s = s.substring(1, s.length()-1);
                String[] parts = s.split(",");
                double[] d = new double[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    try { d[i] = Double.parseDouble(parts[i].trim()); } catch (Exception ex) { d[i] = 0.0; }
                }
                return d;
            }

            private double[] toDouble(float[] f) {
                double[] d = new double[f.length];
                for (int i = 0; i < f.length; i++) d[i] = f[i];
                return d;
            }

            private double cosine(double[] a, double[] b) {
                int n = Math.min(a.length, b.length);
                double dot=0, na=0, nb=0;
                for (int i = 0; i < n; i++) { dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i]; }
                double denom = Math.sqrt(na)*Math.sqrt(nb);
                return denom == 0 ? 0 : dot/denom;
            }
        };
    }
}
