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
}
