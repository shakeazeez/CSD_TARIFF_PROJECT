package com.tariff.calculation.tariffCalc.utility;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;

import java.io.InputStream;

import io.github.cdimascio.dotenv.Dotenv;

public class LemmaUtils {

    private static final DictionaryLemmatizer lemmatizer;
    
    static {
        try {
            
            InputStream dictLemmatizer = LemmaUtils.class.getResourceAsStream("/models/en-lemmatizer.dict");
            lemmatizer = new DictionaryLemmatizer(dictLemmatizer);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load OpenNLP models", e);
        }
    }
    
    public static String getEnvOrDotenv(String key) {
        String value = System.getenv(key);
        if (value != null) return value;
        try {
            Dotenv dotenv = Dotenv.load();
            return dotenv.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the lemma (singular) of a plural noun, or original if the word is already singular
     * Uses the en-lemmatizer.dict from OpenNLP
     */
    public static String toSingular(String nounPlural) {
        if (nounPlural == null || nounPlural.isBlank()) {
            return nounPlural;
        }

    // split phrase into words
    String[] words = nounPlural.split("\\s+");
    StringBuilder singularPhrase = new StringBuilder();

    for (String word : words) { // lemmatization works word by word, not on entire phrases
        
        // treat each word as plural noun for the lemmatizer
        String[] tokens = new String[]{word}; // OpenNLP’s DictionaryLemmatizer expects arrays of tokens, not a single string
        String[] tags = new String[]{"NNS"}; // manually tag as plural (NNS = plural noun)

        // lemmatizer.lemmatize(tokens, tags):
        // - returns an array of lemmas, which is the singular form of each word in our case
        // - if no match is found, it returns "O"
        String[] lemmas = lemmatizer.lemmatize(tokens, tags);
        if (lemmas != null && lemmas.length > 0 && !lemmas[0].equals("O")) {
            singularPhrase.append(lemmas[0]);
        } else {
            singularPhrase.append(word); // just keep the original word if unknown (words that are already singular)
        }

        singularPhrase.append(" ");
    }

        return singularPhrase.toString().trim();
    }
}
