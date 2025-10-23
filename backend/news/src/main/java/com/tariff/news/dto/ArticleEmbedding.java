package com.tariff.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEmbedding {
    private String title;
    private String url;
    private String cleanedText;
    private String embedding; // Stored as string format "[0.123,-0.456,0.789,...]"
    // GPT-produced short context that ties this article to the query/topic (RAG)
    private String queryContext;
    // The last query or topic that caused this article to be saved/updated
    private String lastSeenQuery;
    // Source of this article: "db" for cached DB result, "api" for freshly fetched
    private String source;
}