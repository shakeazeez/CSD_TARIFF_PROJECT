package com.tariff.calculation.tariffCalc.category;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tariff.calculation.tariffCalc.service.EmbeddingService;

/**
 * Comprehensive unit tests for CategoryService
 * Tests all methods, business logic, and exception handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private CategoryService categoryService;

    private Category testTechnologyCategory;
    private Category testManufacturingCategory;
    private Category testOtherCategory;
    private float[] testEmbedding;

    @BeforeEach
    void setUp() {
        testTechnologyCategory = new Category();
        testTechnologyCategory.setId(1);
        testTechnologyCategory.setName("Technology");
        testTechnologyCategory.setDesc("Computer systems, software development, IT infrastructure");
        testTechnologyCategory.setEmbedding(new float[]{0.1f, 0.2f, 0.3f});

        testManufacturingCategory = new Category();
        testManufacturingCategory.setId(2);
        testManufacturingCategory.setName("Manufacturing");
        testManufacturingCategory.setDesc("Industrial production and assembly of mechanical parts");
        testManufacturingCategory.setEmbedding(new float[]{0.4f, 0.5f, 0.6f});

        testOtherCategory = new Category();
        testOtherCategory.setId(3);
        testOtherCategory.setName("Other");
        testOtherCategory.setDesc("Miscellaneous items not fitting other categories");
        testOtherCategory.setEmbedding(null); // Other category should not have embedding

        testEmbedding = new float[1536];
        Arrays.fill(testEmbedding, 0.1f); // Fill with 0.1 for simplicity
    }

    private String createEmbeddingString() {
        // Create the exact string format that the service generates
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < testEmbedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(testEmbedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    // ===== INITIALIZE CATEGORIES TESTS =====

    @Test
    @DisplayName("Should initialize categories when no categories exist")
    void initializeCategories_ShouldInitializeCategories_WhenNoCategoriesExist() {
        // Arrange
        when(categoryRepo.count()).thenReturn(0L);
        when(embeddingService.getEmbedding(anyString())).thenReturn(testEmbedding);

        // Act
        categoryService.initializeCategories();

        // Assert
        verify(categoryRepo, times(1)).count();
        // Verify that save is called for each industry (except 'Other' gets saved without embedding)
        verify(categoryRepo, times(Industry.values().length)).save(any(Category.class));
        // Verify embeddings are generated for non-'Other' industries
        verify(embeddingService, times(Industry.values().length - 1)).getEmbedding(anyString());
    }

    @Test
    @DisplayName("Should skip initialization when categories already exist")
    void initializeCategories_ShouldSkipInitialization_WhenCategoriesAlreadyExist() {
        // Arrange
        List<Category> existingCategories = Arrays.asList(testTechnologyCategory, testManufacturingCategory);
        when(categoryRepo.count()).thenReturn(2L);
        when(categoryRepo.findAll()).thenReturn(existingCategories);

        // Act
        categoryService.initializeCategories();

        // Assert
        verify(categoryRepo, times(1)).count();
        verify(categoryRepo, times(1)).findAll();
        // Should not create new categories
        verify(categoryRepo, never()).save(any(Category.class));
        verify(embeddingService, never()).getEmbedding(anyString());
    }

    @Test
    @DisplayName("Should backfill embeddings for categories missing embeddings")
    void initializeCategories_ShouldBackfillEmbeddings_WhenCategoriesMissingEmbeddings() {
        // Arrange
        Category categoryWithoutEmbedding = new Category();
        categoryWithoutEmbedding.setId(4);
        categoryWithoutEmbedding.setName("Agriculture");
        categoryWithoutEmbedding.setDesc("Farming and crop cultivation");
        categoryWithoutEmbedding.setEmbedding(null);

        List<Category> existingCategories = Arrays.asList(
                testTechnologyCategory, 
                categoryWithoutEmbedding, 
                testOtherCategory
        );
        
        when(categoryRepo.count()).thenReturn(3L);
        when(categoryRepo.findAll()).thenReturn(existingCategories);
        when(embeddingService.getEmbedding("Farming and crop cultivation")).thenReturn(testEmbedding);

        // Act
        categoryService.initializeCategories();

        // Assert
        verify(categoryRepo, times(1)).count();
        verify(categoryRepo, times(1)).findAll();
        // Should save the category that needed backfilling and clear Other's embedding if it had one
        verify(categoryRepo, times(1)).save(categoryWithoutEmbedding);
        verify(embeddingService, times(1)).getEmbedding("Farming and crop cultivation");
    }

    @Test
    @DisplayName("Should clear embedding for Other category if it exists")
    void initializeCategories_ShouldClearOtherEmbedding_WhenOtherHasEmbedding() {
        // Arrange
        Category techWithoutEmbedding = new Category();
        techWithoutEmbedding.setId(1);
        techWithoutEmbedding.setName("Technology");
        techWithoutEmbedding.setDesc("Computer systems, software development, IT infrastructure");
        techWithoutEmbedding.setEmbedding(null); // This category needs backfilling

        Category otherWithEmbedding = new Category();
        otherWithEmbedding.setId(5);
        otherWithEmbedding.setName("Other");
        otherWithEmbedding.setDesc("Miscellaneous items");
        otherWithEmbedding.setEmbedding(new float[]{0.1f, 0.2f}); // Other incorrectly has embedding

        List<Category> existingCategories = Arrays.asList(techWithoutEmbedding, otherWithEmbedding);
        
        when(categoryRepo.count()).thenReturn(2L);
        when(categoryRepo.findAll()).thenReturn(existingCategories);
        when(embeddingService.getEmbedding(anyString())).thenReturn(testEmbedding);

        // Act
        categoryService.initializeCategories();

        // Assert
        verify(categoryRepo, times(1)).count();
        verify(categoryRepo, times(1)).findAll();
        // Verify that save was called twice: once for Other (clearing) and once for Technology (backfilling)
        verify(categoryRepo, times(2)).save(any(Category.class));
        // Verify that save was called with a Category that has "Other" name and null embedding
        verify(categoryRepo, times(1)).save(argThat(category -> 
            "Other".equals(category.getName()) && category.getEmbedding() == null));
        // Verify that save was called with a Category that has "Technology" name and non-null embedding
        verify(categoryRepo, times(1)).save(argThat(category -> 
            "Technology".equals(category.getName()) && category.getEmbedding() != null));
        verify(embeddingService, times(1)).getEmbedding(anyString());
        assertNull(otherWithEmbedding.getEmbedding());
    }

    @Test
    @DisplayName("Should handle exceptions during initialization gracefully")
    void initializeCategories_ShouldHandleExceptions_Gracefully() {
        // Arrange
        when(categoryRepo.count()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> categoryService.initializeCategories());
        
        verify(categoryRepo, times(1)).count();
        verify(categoryRepo, never()).save(any(Category.class));
    }

    // ===== FIND MOST SIMILAR INDUSTRY TESTS =====

    @Test
    @DisplayName("Should return most similar category when description matches")
    void findMostSimilarIndustry_ShouldReturnMostSimilarCategory_WhenDescriptionMatches() {
        // Arrange
        String description = "laptop computer software development";
        // Create the exact embedding string that the service will generate
        String embeddingString = createEmbeddingString();
        List<Category> sortedCategories = Arrays.asList(testTechnologyCategory, testManufacturingCategory);
        List<String> distances = Arrays.asList("0.1", "0.8");
        
        when(embeddingService.getEmbedding(description)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(embeddingString)).thenReturn(sortedCategories);
        when(categoryRepo.findClosestDistances(embeddingString)).thenReturn(distances);

        // Act
        Category result = categoryService.findMostSimilarIndustry(description);

        // Assert
        assertNotNull(result);
        assertEquals(testTechnologyCategory, result);
        assertEquals("Technology", result.getName());
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, times(1)).findClosestCategories(embeddingString);
        verify(categoryRepo, times(1)).findClosestDistances(embeddingString);
    }

    @Test
    @DisplayName("Should return null when no categories found")
    void findMostSimilarIndustry_ShouldReturnNull_WhenNoCategoriesFound() {
        // Arrange
        String description = "unknown item";
        String embeddingString = createEmbeddingString();
        
        when(embeddingService.getEmbedding(description)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(embeddingString)).thenReturn(Collections.emptyList());
        when(categoryRepo.findClosestDistances(embeddingString)).thenReturn(Collections.emptyList());

        // Act
        Category result = categoryService.findMostSimilarIndustry(description);

        // Assert
        assertNull(result);
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, times(1)).findClosestCategories(embeddingString);
        verify(categoryRepo, times(1)).findClosestDistances(embeddingString);
    }

    @Test
    @DisplayName("Should handle null description input")
    void findMostSimilarIndustry_ShouldHandleNullDescription() {
        // Arrange
        String nullDescription = null;
        
        when(embeddingService.getEmbedding(nullDescription)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(anyString())).thenReturn(Arrays.asList(testOtherCategory));
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(Arrays.asList("0.5"));

        // Act
        Category result = categoryService.findMostSimilarIndustry(nullDescription);

        // Assert
        assertNotNull(result);
        assertEquals(testOtherCategory, result);
        verify(embeddingService, times(1)).getEmbedding(nullDescription);
    }

    @Test
    @DisplayName("Should handle empty description input")
    void findMostSimilarIndustry_ShouldHandleEmptyDescription() {
        // Arrange
        String emptyDescription = "   ";
        
        when(embeddingService.getEmbedding(emptyDescription)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(anyString())).thenReturn(Arrays.asList(testOtherCategory));
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(Arrays.asList("0.5"));

        // Act
        Category result = categoryService.findMostSimilarIndustry(emptyDescription);

        // Assert
        assertNotNull(result);
        verify(embeddingService, times(1)).getEmbedding(emptyDescription);
    }

    @Test
    @DisplayName("Should handle multiple categories with different distances")
    void findMostSimilarIndustry_ShouldHandleMultipleCategoriesWithDistances() {
        // Arrange
        String description = "factory automation equipment";
        String embeddingString = createEmbeddingString();
        List<Category> sortedCategories = Arrays.asList(
                testManufacturingCategory, 
                testTechnologyCategory, 
                testOtherCategory
        );
        List<String> distances = Arrays.asList("0.2", "0.6", "0.9");
        
        when(embeddingService.getEmbedding(description)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(embeddingString)).thenReturn(sortedCategories);
        when(categoryRepo.findClosestDistances(embeddingString)).thenReturn(distances);

        // Act
        Category result = categoryService.findMostSimilarIndustry(description);

        // Assert
        assertNotNull(result);
        assertEquals(testManufacturingCategory, result);
        assertEquals("Manufacturing", result.getName());
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, times(1)).findClosestCategories(embeddingString);
        verify(categoryRepo, times(1)).findClosestDistances(embeddingString);
    }

    @Test
    @DisplayName("Should handle embedding service exceptions")
    void findMostSimilarIndustry_ShouldHandleEmbeddingServiceExceptions() {
        // Arrange
        String description = "test description";
        when(embeddingService.getEmbedding(description)).thenThrow(new RuntimeException("API error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.findMostSimilarIndustry(description));
        
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, never()).findClosestCategories(anyString());
    }

    @Test
    @DisplayName("Should handle database query exceptions")
    void findMostSimilarIndustry_ShouldHandleDatabaseExceptions() {
        // Arrange
        String description = "test description";
        String embeddingString = createEmbeddingString();
        
        when(embeddingService.getEmbedding(description)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(embeddingString)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.findMostSimilarIndustry(description));
        
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, times(1)).findClosestCategories(embeddingString);
    }

    @Test
    @DisplayName("Should format embedding array correctly for database query")
    void findMostSimilarIndustry_ShouldFormatEmbeddingCorrectly() {
        // Arrange
        String description = "test description";
        float[] largeEmbedding = new float[1536]; // Typical embedding size
        Arrays.fill(largeEmbedding, 0.5f);
        
        when(embeddingService.getEmbedding(description)).thenReturn(largeEmbedding);
        when(categoryRepo.findClosestCategories(anyString())).thenReturn(Arrays.asList(testTechnologyCategory));
        when(categoryRepo.findClosestDistances(anyString())).thenReturn(Arrays.asList("0.3"));

        // Act
        Category result = categoryService.findMostSimilarIndustry(description);

        // Assert
        assertNotNull(result);
        verify(embeddingService, times(1)).getEmbedding(description);
        
        // Verify that the embedding string starts and ends with brackets
        verify(categoryRepo, times(1)).findClosestCategories(argThat(s -> s.startsWith("[") && s.endsWith("]")));
        verify(categoryRepo, times(1)).findClosestDistances(argThat(s -> s.startsWith("[") && s.endsWith("]")));
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("Should handle mismatched categories and distances lists")
    void findMostSimilarIndustry_ShouldHandleMismatchedLists() {
        // Arrange
        String description = "test description";
        String embeddingString = createEmbeddingString();
        List<Category> categories = Arrays.asList(testTechnologyCategory, testManufacturingCategory);
        List<String> distances = Arrays.asList("0.1"); // Only one distance for two categories
        
        when(embeddingService.getEmbedding(description)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(embeddingString)).thenReturn(categories);
        when(categoryRepo.findClosestDistances(embeddingString)).thenReturn(distances);

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> 
            categoryService.findMostSimilarIndustry(description));
        
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, times(1)).findClosestCategories(embeddingString);
        verify(categoryRepo, times(1)).findClosestDistances(embeddingString);
    }

    @Test
    @DisplayName("Should handle invalid distance strings")
    void findMostSimilarIndustry_ShouldHandleInvalidDistanceStrings() {
        // Arrange
        String description = "test description";
        String embeddingString = createEmbeddingString();
        List<Category> categories = Arrays.asList(testTechnologyCategory);
        List<String> invalidDistances = Arrays.asList("not_a_number");
        
        when(embeddingService.getEmbedding(description)).thenReturn(testEmbedding);
        when(categoryRepo.findClosestCategories(embeddingString)).thenReturn(categories);
        when(categoryRepo.findClosestDistances(embeddingString)).thenReturn(invalidDistances);

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> categoryService.findMostSimilarIndustry(description));
        
        verify(embeddingService, times(1)).getEmbedding(description);
        verify(categoryRepo, times(1)).findClosestCategories(embeddingString);
        verify(categoryRepo, times(1)).findClosestDistances(embeddingString);
    }
}