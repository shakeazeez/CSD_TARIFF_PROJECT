# NewsEmbeddingService Test Suite

This directory contains comprehensive tests for the `NewsEmbeddingService` that minimize external API calls by using mocks and stubs.

## Test Structure

### 1. Unit Tests (`NewsEmbeddingServiceTest.java`)
- **Purpose**: Tests individual methods and core functionality
- **Approach**: Heavy mocking of external dependencies (WebClient, Repository)
- **Key Features**:
  - Tests database hit scenarios (when similarity threshold is met)
  - Tests API fallback scenarios (when database miss occurs)
  - Tests error handling and edge cases
  - Minimal external API calls (all mocked)

### 2. Utility Tests (`NewsEmbeddingServiceUtilityTest.java`)
- **Purpose**: Tests utility methods that don't require external dependencies
- **Approach**: Direct testing without mocks
- **Key Features**:
  - String to embedding conversion
  - Float array manipulation
  - Article class functionality
  - Input validation and error handling

### 3. Integration Tests (`NewsEmbeddingServiceIntegrationTest.java`)
- **Purpose**: Tests end-to-end workflows with realistic data
- **Approach**: Spring Boot test context with mocked external services
- **Key Features**:
  - Full workflow testing (database → API fallback)
  - Performance testing with large datasets
  - Configuration validation
  - Error handling in realistic scenarios

### 4. Repository Tests (`ArticleEmbeddingRepoTest.java`)
- **Purpose**: Tests repository layer interactions
- **Approach**: Mocked repository with expected behaviors
- **Key Features**:
  - CRUD operations
  - Custom query methods
  - Vector similarity operations

## Running the Tests

### Prerequisites
1. Java 21
2. Maven 3.6+
3. Spring Boot 3.4.9

### Run All Tests
```bash
cd /Users/josephyau/Documents/Projects/CSD_TARIFF_PROJECT/backend/news
mvn test
```

### Run Specific Test Classes
```bash
# Unit tests only
mvn test -Dtest=NewsEmbeddingServiceTest

# Integration tests only
mvn test -Dtest=NewsEmbeddingServiceIntegrationTest

# Utility tests only
mvn test -Dtest=NewsEmbeddingServiceUtilityTest

# Repository tests only
mvn test -Dtest=ArticleEmbeddingRepoTest
```

### Run Test Suite
```bash
mvn test -Dtest=NewsEmbeddingServiceTestSuite
```

## Test Configuration

### Properties
- **Test Profile**: Uses `application-test.properties`
- **Database**: H2 in-memory database for testing
- **API Keys**: Dummy values for testing (no real API calls)
- **Logging**: Debug level for detailed test output

### Mock Strategy
1. **WebClient**: Fully mocked to prevent external HTTP calls
2. **ObjectMapper**: Real instance for JSON processing
3. **Repository**: Mocked with predefined responses
4. **External APIs**: All responses are stubbed

## Test Coverage

### Scenarios Covered
1. **Database Hit Path**:
   - High similarity articles found in database
   - Returns cached results without API calls
   - Proper synthesis of answers

2. **API Fallback Path**:
   - Low/no similarity in database
   - Falls back to external news API
   - Processes and stores new articles
   - Handles API failures gracefully

3. **Error Handling**:
   - Database connection failures
   - External API unavailability
   - Invalid input handling
   - Network timeouts

4. **Performance**:
   - Large dataset processing
   - Memory usage optimization
   - Response time validation

5. **Configuration**:
   - Property injection
   - Threshold configurations
   - Feature toggles (pgvector, etc.)

## Key Testing Principles

### 1. No External Dependencies
- All external API calls are mocked
- No real OpenAI API calls
- No real news API calls
- No database persistence required

### 2. Realistic Data
- Uses actual embedding dimensions (1536)
- Realistic article content and structures
- Proper JSON response formats
- Valid URL and text patterns

### 3. Comprehensive Coverage
- Happy path scenarios
- Error conditions
- Edge cases
- Performance scenarios

### 4. Fast Execution
- All tests should complete in under 30 seconds
- No network I/O delays
- Minimal setup/teardown

## Mock Data Patterns

### Embeddings
- 1536-dimensional vectors (matching OpenAI ada-002)
- Gaussian distribution around 0 with std dev 0.1
- Fixed random seed for reproducibility

### Articles
- Realistic titles and URLs
- Trade-focused content for domain relevance
- Proper metadata (topic, context, etc.)
- Various similarity scores for testing

### API Responses
- Valid JSON structures matching real APIs
- Error responses for failure testing
- Empty responses for edge cases

## Debugging Tests

### Enable Debug Logging
Add to `application-test.properties`:
```properties
logging.level.com.tariff.news=TRACE
logging.level.org.mockito=DEBUG
```

### Common Issues
1. **Mock not working**: Check method signatures match exactly
2. **Test timeout**: Verify no real network calls are being made
3. **JSON parsing errors**: Ensure mock responses match expected format
4. **Random failures**: Check for proper test isolation

## Future Enhancements

### Potential Additions
1. **Contract Testing**: Verify mock responses match real API contracts
2. **Load Testing**: Performance tests with concurrent requests
3. **Security Testing**: Input validation and injection prevention
4. **Chaos Testing**: Random failure injection

### Test Data Management
1. **Test Fixtures**: Centralized test data management
2. **Data Builders**: Fluent APIs for test data creation
3. **Snapshot Testing**: Compare outputs against known good results

## Maintenance

### Regular Tasks
1. Update mock responses when APIs change
2. Refresh test data to match production patterns
3. Review and update performance thresholds
4. Validate mock behaviors against real services

### When to Update Tests
- API contract changes
- New features added
- Configuration changes
- Performance requirements change
