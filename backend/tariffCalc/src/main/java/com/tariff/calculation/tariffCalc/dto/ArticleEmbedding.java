package com.tariff.calculation.tariffCalc.dto;

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
}