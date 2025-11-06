package com.tariff.news.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloatArrayConverterTest {

    private FloatArrayConverter converter;

    @BeforeEach
    void setUp() {
        converter = new FloatArrayConverter();
    }

    // ===== convertToDatabaseColumn tests =====
    
    @Test
    void convertToDatabaseColumn_WithNullArray_ReturnsNull() {
        // Arrange
        float[] inputArray = null;

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertNull(result);
    }

    @Test
    void convertToDatabaseColumn_WithEmptyArray_ReturnsEmptyBrackets() {
        // Arrange
        float[] inputArray = {};

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertEquals("[]", result);
    }

    @Test
    void convertToDatabaseColumn_WithSingleElement_ReturnsCorrectFormat() {
        // Arrange
        float[] inputArray = {1.5f};

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertEquals("[1.5]", result);
    }

    @Test
    void convertToDatabaseColumn_WithMultipleElements_ReturnsCorrectFormat() {
        // Arrange
        float[] inputArray = {1.0f, 2.5f, 3.7f};

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertEquals("[1.0,2.5,3.7]", result);
    }

    @Test
    void convertToDatabaseColumn_WithNegativeNumbers_ReturnsCorrectFormat() {
        // Arrange
        float[] inputArray = {-1.5f, 0.0f, -3.7f};

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertEquals("[-1.5,0.0,-3.7]", result);
    }

    @Test
    void convertToDatabaseColumn_WithVerySmallNumbers_ReturnsCorrectFormat() {
        // Arrange
        float[] inputArray = {0.000001f, 1e-6f};

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertTrue(result.contains("1.0E-6") || result.contains("0.000001"));
    }

    @Test
    void convertToDatabaseColumn_WithVeryLargeNumbers_ReturnsCorrectFormat() {
        // Arrange
        float[] inputArray = {1000000.0f, Float.MAX_VALUE};

        // Act
        String result = converter.convertToDatabaseColumn(inputArray);

        // Assert
        assertTrue(result.startsWith("[") && result.endsWith("]"));
        assertTrue(result.contains("1000000.0"));
    }

    // ===== convertToEntityAttribute tests =====
    
    @Test
    void convertToEntityAttribute_WithNullString_ReturnsNull() {
        // Arrange
        String inputString = null;

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute_WithEmptyBrackets_ReturnsEmptyArray() {
        // Arrange
        String inputString = "[]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void convertToEntityAttribute_WithWhitespaceOnlyBrackets_ReturnsEmptyArray() {
        // Arrange
        String inputString = "[ ]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void convertToEntityAttribute_WithSingleElement_ReturnsCorrectArray() {
        // Arrange
        String inputString = "[1.5]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(1.5f, result[0], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithMultipleElements_ReturnsCorrectArray() {
        // Arrange
        String inputString = "[1.0,2.5,3.7]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1.0f, result[0], 0.001f);
        assertEquals(2.5f, result[1], 0.001f);
        assertEquals(3.7f, result[2], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithSpacesAroundCommas_ReturnsCorrectArray() {
        // Arrange
        String inputString = "[1.0, 2.5 , 3.7]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1.0f, result[0], 0.001f);
        assertEquals(2.5f, result[1], 0.001f);
        assertEquals(3.7f, result[2], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithNegativeNumbers_ReturnsCorrectArray() {
        // Arrange
        String inputString = "[-1.5,0.0,-3.7]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(-1.5f, result[0], 0.001f);
        assertEquals(0.0f, result[1], 0.001f);
        assertEquals(-3.7f, result[2], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithInvalidNumber_SetsToZero() {
        // Arrange
        String inputString = "[1.5,invalid,3.7]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(1.5f, result[0], 0.001f);
        assertEquals(0.0f, result[1], 0.001f); // invalid number becomes 0.0
        assertEquals(3.7f, result[2], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithMultipleInvalidNumbers_SetsAllToZero() {
        // Arrange
        String inputString = "[abc,def,ghi]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(0.0f, result[0], 0.001f);
        assertEquals(0.0f, result[1], 0.001f);
        assertEquals(0.0f, result[2], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithScientificNotation_ReturnsCorrectArray() {
        // Arrange
        String inputString = "[1e-6,1.5E3]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(1e-6f, result[0], 0.000001f);
        assertEquals(1500.0f, result[1], 0.001f);
    }

    @Test
    void convertToEntityAttribute_WithVeryShortString_ReturnsEmptyArray() {
        // Arrange
        String inputString = "[";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void convertToEntityAttribute_WithSingleCharacter_ReturnsEmptyArray() {
        // Arrange
        String inputString = "x";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void convertToEntityAttribute_WithJustBrackets_ReturnsEmptyArray() {
        // Arrange
        String inputString = "[]";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void convertToEntityAttribute_WithExtraWhitespace_ReturnsCorrectArray() {
        // Arrange
        String inputString = "  [  1.0  ,  2.5  ]  ";

        // Act
        float[] result = converter.convertToEntityAttribute(inputString);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(1.0f, result[0], 0.001f);
        assertEquals(2.5f, result[1], 0.001f);
    }
}