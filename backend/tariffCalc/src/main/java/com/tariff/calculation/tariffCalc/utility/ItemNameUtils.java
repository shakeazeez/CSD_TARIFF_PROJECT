package com.tariff.calculation.tariffCalc.utility;

import org.atteo.evo.inflector.English;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for converting plural item names to singular
 * Contains 2 public methods:
 * - pluralToSingular(String pluralWord): converts a plural noun to its singular form
 * - isPlural(String word): checks if a word is plural using EVO Inflector
 * Uses EVO Inflector library and custom rules for better accuracy
 */
public class ItemNameUtils {
    
    // Custom irregular plurals that the library might miss
    private static final Map<String, String> CUSTOM_IRREGULAR_PLURALS = new HashMap<>();
    
    // Manual pluralization rules for better accuracy
    private static final Map<String, String> COMMON_PLURALS = new HashMap<>();
    
    static {
        // Add common irregular plurals
        COMMON_PLURALS.put("children", "child");
        COMMON_PLURALS.put("men", "man");
        COMMON_PLURALS.put("women", "woman");
        COMMON_PLURALS.put("feet", "foot");
        COMMON_PLURALS.put("mice", "mouse");
        COMMON_PLURALS.put("geese", "goose");
        COMMON_PLURALS.put("teeth", "tooth");
        COMMON_PLURALS.put("people", "person");
        COMMON_PLURALS.put("oxen", "ox");
        
        // Add any custom mappings if needed
        CUSTOM_IRREGULAR_PLURALS.put("equipment", "equipment");
        CUSTOM_IRREGULAR_PLURALS.put("software", "software");
        CUSTOM_IRREGULAR_PLURALS.put("hardware", "hardware");
        CUSTOM_IRREGULAR_PLURALS.put("furniture", "furniture");
        CUSTOM_IRREGULAR_PLURALS.put("information", "information");
        CUSTOM_IRREGULAR_PLURALS.put("data", "datum"); // though commonly used as singular
        CUSTOM_IRREGULAR_PLURALS.put("sheep", "sheep");
        CUSTOM_IRREGULAR_PLURALS.put("deer", "deer");
        CUSTOM_IRREGULAR_PLURALS.put("fish", "fish");
    }
    
    /**
     * Converts a plural noun to its singular form
     * @param pluralWord the plural word to convert
     * @return the singular form of the word
     */
    public static String pluralToSingular(String pluralWord) {
        if (pluralWord == null || pluralWord.trim().isEmpty()) {
            return pluralWord;
        }
        
        String trimmed = pluralWord.trim();
        
        // Handle compound words (e.g., "tennis shoes" -> "tennis shoe")
        if (trimmed.contains(" ")) {
            return singulariseCompoundWord(trimmed);
        }
        
        String lowerCase = trimmed.toLowerCase();
        
        // Check common irregular plurals first
        if (COMMON_PLURALS.containsKey(lowerCase)) {
            return COMMON_PLURALS.get(lowerCase);
        }
        
        // Check custom irregular plurals
        if (CUSTOM_IRREGULAR_PLURALS.containsKey(lowerCase)) {
            return CUSTOM_IRREGULAR_PLURALS.get(lowerCase);
        }
        
        try {
            // Use manual rules for common patterns first
            String manualSingular = applySingularisationRules(lowerCase);
            if (!manualSingular.equals(lowerCase)) {
                return manualSingular;
            }
            
            // Try EVO Inflector as fallback to check if it's actually plural
            String testPlural = English.plural(lowerCase);
            if (!testPlural.equals(lowerCase)) {
                // If pluralising the word gives a different result, the word is likely singular already
                return lowerCase;
            }
            
            // If the word seems plural, apply manual rules
            return manualSingular;
            
        } catch (Exception e) {
            // Fallback to manual rules
            return applySingularisationRules(lowerCase);
        }
    }
    
    /**
     * Apply manual singularisation rules
     */
    private static String applySingularisationRules(String word) {
        // Already checked irregular plurals above
        
        // Rule for words ending in -ies -> -y
        if (word.endsWith("ies") && word.length() > 3) {
            return word.substring(0, word.length() - 3) + "y";
        }
        
        // Rule for words ending in -ves -> -f or -fe
        if (word.endsWith("ves") && word.length() > 3) {
            String base = word.substring(0, word.length() - 3);
            // Common -ves words
            if (base.equals("lea") || base.equals("wi") || base.equals("kni") || 
                base.equals("li") || base.equals("shel") || base.equals("ther")) {
                return base + "f";
            }
            return base + "fe"; // Default to -fe
        }
        
        // Rule for words ending in -es
        if (word.endsWith("es") && word.length() > 2) {
            String withoutEs = word.substring(0, word.length() - 2);
            
            // Words ending in -ches, -shes, -xes, -zes -> remove -es
            if (withoutEs.endsWith("ch") || withoutEs.endsWith("sh") || 
                withoutEs.endsWith("x") || withoutEs.endsWith("z") ||
                withoutEs.endsWith("s")) {
                return withoutEs;
            }
            
            // Check if removing just 's' makes sense
            String withoutS = word.substring(0, word.length() - 1);
            if (withoutS.endsWith("s")) {
                return withoutEs; // Remove 'es'
            }
            
            return withoutS; // Just remove 's'
        }
        
        // Rule for regular plurals ending in -s (but not -ss)
        if (word.endsWith("s") && word.length() > 1 && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        
        // If no rules apply, return as is
        return word;
    }
    
    /**
     * Handle compound words like "tennis shoes" -> "tennis shoe"
     */
    private static String singulariseCompoundWord(String compoundWord) {
        String[] words = compoundWord.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            
            // Only singularise the last word if it's likely a noun
            if (i == words.length - 1) {
                result.append(pluralToSingular(words[i]));
            } else {
                result.append(words[i]);
            }
        }
        
        return result.toString();
    }
    
    /**
     * Check if a word is likely plural
     */
    public static boolean isPlural(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        String singular = pluralToSingular(word.trim());
        return !singular.equalsIgnoreCase(word.trim());
    }
}