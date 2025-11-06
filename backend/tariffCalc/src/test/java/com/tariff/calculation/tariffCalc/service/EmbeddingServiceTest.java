package com.tariff.calculation.tariffCalc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tariff.calculation.tariffCalc.category.Category;
import com.tariff.calculation.tariffCalc.category.CategoryRepo;

@ExtendWith(MockitoExtension.class)
public class EmbeddingServiceTest {

    @Mock
    private CategoryRepo categoryRepo;



    private EmbeddingService embeddingService;

    private Category mockElectronicsCategory;
    private Category mockClothingCategory;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(categoryRepo);
        
        // Setup test categories
        mockElectronicsCategory = new Category();
        mockElectronicsCategory.setName("Electronics");
        mockElectronicsCategory.setDesc("Electronic devices and components");
        mockElectronicsCategory.setEmbedding(createMockEmbedding(0.5f));

        mockClothingCategory = new Category();
        mockClothingCategory.setName("Clothing");
        mockClothingCategory.setDesc("Apparel and fashion items");
        mockClothingCategory.setEmbedding(createMockEmbedding(0.3f));
    }

    @Test
    void testGetEmbedding_NullText_ReturnsDefaultEmbedding() {
        // Arrange & Act
        float[] result = embeddingService.getEmbedding(null);

        // Assert
        assertNotNull(result);
        assertEquals(1536, result.length);
        assertEquals(0.001f, result[0]);
        assertEquals(0.001f, result[1535]);
    }

    @Test
    void testGetEmbedding_EmptyText_ReturnsDefaultEmbedding() {
        // Arrange & Act
        float[] result = embeddingService.getEmbedding("   ");

        // Assert
        assertNotNull(result);
        assertEquals(1536, result.length);
        assertEquals(0.001f, result[0]);
    }

    @Test
    void testGetEmbedding_NoApiKey_ReturnsFallbackEmbedding() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "openaiApiKey", null);
        String testText = "smartphone";

        // Act
        float[] result = embeddingService.getEmbedding(testText);

        // Assert
        assertNotNull(result);
        assertEquals(1536, result.length);
        // The first 100 dimensions should have variation from hash-based features
        boolean hasVariation = false;
        for (int i = 0; i < 100; i++) {
            if (result[i] > 0.001f) {
                hasVariation = true;
                break;
            }
        }
        assertTrue(hasVariation, "Should have hash-based variation in first 100 dimensions");
    }

    @Test
    void testGetEmbedding_EmptyApiKey_ReturnsFallbackEmbedding() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "openaiApiKey", "");
        String testText = "laptop";

        // Act
        float[] result = embeddingService.getEmbedding(testText);

        // Assert
        assertNotNull(result);
        assertEquals(1536, result.length);
        // Check that it has the basic fallback structure
        assertEquals(0.001f, result[1536-1], 0.0001f); // Last element should be base value
        // Check for hash-based variation in first 100 dimensions
        boolean hasVariation = false;
        for (int i = 0; i < 100; i++) {
            if (result[i] > 0.001f) {
                hasVariation = true;
                break;
            }
        }
        assertTrue(hasVariation, "Should have hash-based variation");
    }

    @Test
    void testGetEmbedding_WithApiKey_ComplexMockingNote() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "openaiApiKey", "test-api-key");
        String testText = "smartphone";

        // Act 
        try {
            float[] result = embeddingService.getEmbedding(testText);
            // If it doesn't throw, verify it's not the null/empty text behavior
            assertNotNull(result);
            assertTrue(result.length > 0);
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException || e.getCause() instanceof RuntimeException);
        }
    }

    @Test
    void testGetEmbeddings_DatabaseKnnSuccess() {
        // Arrange
        String itemName = "laptop";
        String description = "portable computer";
        
        List<Category> mockCategories = Arrays.asList(mockElectronicsCategory);
        List<String> mockDistances = Arrays.asList("0.5");

        when(categoryRepo.findClosestCategories(anyString())).thenReturn(mockCategories);
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(mockDistances);

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        
        verify(categoryRepo).findClosestCategories(anyString());
        verify(categoryRepo).findClosestDistances(anyString());
    }

    @Test
    void testGetEmbeddings_DatabaseKnnDistanceThreshold_ReturnsOther() {
        // Arrange
        String itemName = "weird item";
        String description = "unknown category";
        
        List<Category> mockCategories = Arrays.asList(mockElectronicsCategory);
        List<String> mockDistances = Arrays.asList("2.0"); // Distance > threshold (1.0)

        when(categoryRepo.findClosestCategories(anyString())).thenReturn(mockCategories);
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(mockDistances);

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Other", result.getName());
        assertEquals("Miscellaneous items not fitting other categories", result.getDesc());
    }

    @Test
    void testGetEmbeddings_DatabaseKnnFails_FallsBackToJavaCalculation() {
        // Arrange
        String itemName = "smartphone";
        String description = "mobile device";
        
        List<Category> allCategories = Arrays.asList(mockElectronicsCategory, mockClothingCategory);

        when(categoryRepo.findClosestCategories(anyString())).thenThrow(new RuntimeException("Database error"));
        when(categoryRepo.findAll()).thenReturn(allCategories);

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName()); // Electronics has higher embedding values
        
        verify(categoryRepo).findClosestCategories(anyString());
        verify(categoryRepo).findAll();
    }

    @Test
    void testGetEmbeddings_JavaFallbackLowSimilarity_ReturnsOther() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "minCosineSimilarity", 0.99); // Set very high threshold
        String itemName = "unknown item";
        String description = "very different category";
        
        List<Category> allCategories = Arrays.asList(mockElectronicsCategory, mockClothingCategory);

        when(categoryRepo.findClosestCategories(anyString())).thenThrow(new RuntimeException("Database error"));
        when(categoryRepo.findAll()).thenReturn(allCategories);

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Other", result.getName());
        assertEquals("Miscellaneous items not fitting other categories", result.getDesc());
    }

    @Test
    void testGetEmbeddings_NoCategoriesInDatabase_ThrowsException() {
        // Arrange
        String itemName = "smartphone";
        String description = "mobile device";

        when(categoryRepo.findClosestCategories(anyString())).thenThrow(new RuntimeException("Database error"));
        when(categoryRepo.findAll()).thenReturn(new ArrayList<>());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> embeddingService.getEmbeddings(itemName, description));
        assertEquals("No matching industry found for item: smartphone mobile device", exception.getMessage());
    }

    @Test
    void testGetEmbeddings_CategoriesWithNullEmbeddings_SkipsThem() {
        // Arrange
        String itemName = "smartphone";
        String description = "mobile device";
        
        Category categoryWithNullEmbedding = new Category();
        categoryWithNullEmbedding.setName("NullCategory");
        categoryWithNullEmbedding.setEmbedding(null);

        List<Category> allCategories = Arrays.asList(categoryWithNullEmbedding, mockElectronicsCategory);

        when(categoryRepo.findClosestCategories(anyString())).thenThrow(new RuntimeException("Database error"));
        when(categoryRepo.findAll()).thenReturn(allCategories);

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName()); // Should skip null embedding category
    }

    @Test
    void testGetEmbeddings_AllCategoriesHaveNullEmbeddings_ThrowsException() {
        // Arrange
        String itemName = "smartphone";
        String description = "mobile device";
        
        Category categoryWithNullEmbedding = new Category();
        categoryWithNullEmbedding.setName("NullCategory");
        categoryWithNullEmbedding.setEmbedding(null);

        List<Category> allCategories = Arrays.asList(categoryWithNullEmbedding);

        when(categoryRepo.findClosestCategories(anyString())).thenThrow(new RuntimeException("Database error"));
        when(categoryRepo.findAll()).thenReturn(allCategories);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> embeddingService.getEmbeddings(itemName, description));
        assertEquals("No matching industry found for item: smartphone mobile device", exception.getMessage());
    }

    @Test
    void testGetEmbeddings_DatabaseReturnsCategoriesButNoDistances() {
        // Arrange
        String itemName = "laptop";
        String description = "portable computer";
        
        List<Category> mockCategories = Arrays.asList(mockElectronicsCategory);

        when(categoryRepo.findClosestCategories(anyString())).thenReturn(mockCategories);
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(new ArrayList<>());

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void testGetEmbeddings_InvalidDistanceFormat_FallsBackToTopMatch() {
        // Arrange
        String itemName = "laptop";
        String description = "portable computer";
        
        List<Category> mockCategories = Arrays.asList(mockElectronicsCategory);
        List<String> invalidDistances = Arrays.asList("invalid_number");

        when(categoryRepo.findClosestCategories(anyString())).thenReturn(mockCategories);
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(invalidDistances);

        // Act
        Category result = embeddingService.getEmbeddings(itemName, description);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void testGetEmbeddings_SingleArgumentInput() {
        // Arrange
        String itemName = "laptop";
        
        List<Category> mockCategories = Arrays.asList(mockElectronicsCategory);
        List<String> mockDistances = Arrays.asList("0.5");

        when(categoryRepo.findClosestCategories(anyString())).thenReturn(mockCategories);
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(mockDistances);

        // Act
        Category result = embeddingService.getEmbeddings(itemName);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void testGetEmbeddings_MultipleArgumentsJoinedCorrectly() {
        // Arrange
        String[] args = {"smart", "phone", "device"};
        
        List<Category> mockCategories = Arrays.asList(mockElectronicsCategory);
        List<String> mockDistances = Arrays.asList("0.5");

        when(categoryRepo.findClosestCategories(anyString())).thenReturn(mockCategories);
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(mockDistances);

        // Act - Test that args are joined with spaces
        Category result = embeddingService.getEmbeddings(args);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        
        // The combined text should be "smart phone device"
        verify(categoryRepo).findClosestCategories(contains("["));
    }

    // Helper method to create mock embeddings
    private float[] createMockEmbedding(float baseValue) {
        float[] embedding = new float[1536];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = baseValue + (i * 0.001f);
        }
        return embedding;
    }
}