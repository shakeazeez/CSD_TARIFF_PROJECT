package com.tariff.news.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for ChatHistoryService following AAA pattern
 * Testing all methods with various scenarios for branch coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatHistory Service Tests")
class ChatHistoryServiceTest {

    @Mock
    private ChatHistoryRepo repo;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatHistoryService chatHistoryService;

    private ChatHistory mockChatHistory;
    private List<Object> mockArticles;

    @BeforeEach
    void setUp() {
        mockChatHistory = new ChatHistory();
        mockChatHistory.setId(1L);
        mockChatHistory.setUsername("testuser");
        mockChatHistory.setTopic("trade policy");
        mockChatHistory.setMessages("[]");

        mockArticles = Arrays.asList(
            createMockArticle("Article 1", "https://example.com/1"),
            createMockArticle("Article 2", "https://example.com/2")
        );
    }

    @Test
    void testSave_NewConversation_CreatesNewChatHistory() throws Exception {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "What is trade policy?";
        String synthesizedAnswer = "Trade policy refers to...";
        Long conversationId = null;

        List<Map<String, Object>> expectedMessages = new ArrayList<>();
        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("query", queryText);
        newMsg.put("response", synthesizedAnswer);
        newMsg.put("sources", mockArticles);
        expectedMessages.add(newMsg);

        when(objectMapper.writeValueAsString(expectedMessages)).thenReturn("[{\"query\":\"What is trade policy?\",\"response\":\"Trade policy refers to...\"}]");
        when(repo.save(any(ChatHistory.class))).thenReturn(mockChatHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, mockArticles, conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatHistory.getId(), result.getId());
        
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(any(ChatHistory.class));
        verify(repo, never()).findById(any());
    }

    @Test
    void testSave_ExistingConversationWithEmptyMessages_AppendsMessage() throws Exception {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "Follow-up question";
        String synthesizedAnswer = "Answer to follow-up";
        Long conversationId = 1L;

        ChatHistory existingHistory = new ChatHistory();
        existingHistory.setId(conversationId);
        existingHistory.setUsername(username);
        existingHistory.setTopic(topic);
        existingHistory.setMessages("");

        List<Map<String, Object>> expectedMessages = new ArrayList<>();
        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("query", queryText);
        newMsg.put("response", synthesizedAnswer);
        newMsg.put("sources", mockArticles);
        expectedMessages.add(newMsg);

        when(repo.findById(conversationId)).thenReturn(Optional.of(existingHistory));
        when(objectMapper.writeValueAsString(expectedMessages)).thenReturn("[{\"query\":\"Follow-up question\"}]");
        when(repo.save(existingHistory)).thenReturn(existingHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, mockArticles, conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(conversationId, result.getId());
        
        verify(repo).findById(conversationId);
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(existingHistory);
    }

    @Test
    void testSave_ExistingConversationWithNullMessages_AppendsMessage() throws Exception {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "Follow-up question";
        String synthesizedAnswer = "Answer to follow-up";
        Long conversationId = 1L;

        ChatHistory existingHistory = new ChatHistory();
        existingHistory.setId(conversationId);
        existingHistory.setUsername(username);
        existingHistory.setTopic(topic);
        existingHistory.setMessages(null);

        when(repo.findById(conversationId)).thenReturn(Optional.of(existingHistory));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{\"query\":\"Follow-up question\"}]");
        when(repo.save(existingHistory)).thenReturn(existingHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, mockArticles, conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(conversationId, result.getId());
        
        verify(repo).findById(conversationId);
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(existingHistory);
    }

    @Test
    void testSave_ExistingConversationWithMessages_AppendsToExisting() throws Exception {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "Follow-up question";
        String synthesizedAnswer = "Answer to follow-up";
        Long conversationId = 1L;

        ChatHistory existingHistory = new ChatHistory();
        existingHistory.setId(conversationId);
        existingHistory.setUsername(username);
        existingHistory.setTopic(topic);
        existingHistory.setMessages("[{\"query\":\"Previous question\",\"response\":\"Previous answer\"}]");

        List<Map<String, Object>> existingMessages = new ArrayList<>();
        Map<String, Object> existingMsg = new HashMap<>();
        existingMsg.put("query", "Previous question");
        existingMsg.put("response", "Previous answer");
        existingMessages.add(existingMsg);

        List<Map<String, Object>> updatedMessages = new ArrayList<>(existingMessages);
        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("query", queryText);
        newMsg.put("response", synthesizedAnswer);
        newMsg.put("sources", mockArticles);
        updatedMessages.add(newMsg);

        when(repo.findById(conversationId)).thenReturn(Optional.of(existingHistory));
        when(objectMapper.readValue(eq(existingHistory.getMessages()), any(TypeReference.class))).thenReturn(existingMessages);
        when(objectMapper.writeValueAsString(updatedMessages)).thenReturn("[{\"query\":\"Previous question\"},{\"query\":\"Follow-up question\"}]");
        when(repo.save(existingHistory)).thenReturn(existingHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, mockArticles, conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(conversationId, result.getId());
        
        verify(repo).findById(conversationId);
        verify(objectMapper).readValue(anyString(), any(TypeReference.class));
        verify(objectMapper).writeValueAsString(updatedMessages);
        verify(repo).save(existingHistory);
    }

    @Test
    void testSave_ConversationNotFound_CreatesNew() throws Exception {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "Question for missing conversation";
        String synthesizedAnswer = "Answer for new conversation";
        Long conversationId = 999L;

        when(repo.findById(conversationId)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("[{\"query\":\"Question for missing conversation\"}]");
        when(repo.save(any(ChatHistory.class))).thenReturn(mockChatHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, mockArticles, conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatHistory.getId(), result.getId());
        
        verify(repo).findById(conversationId);
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(any(ChatHistory.class));
    }

    @Test
    void testSave_JsonProcessingException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "What is trade policy?";
        String synthesizedAnswer = "Trade policy refers to...";
        Long conversationId = null;

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            chatHistoryService.save(username, topic, queryText, synthesizedAnswer, mockArticles, conversationId);
        });
        
        verify(objectMapper).writeValueAsString(any());
        verify(repo, never()).save(any());
    }

    @Test
    void testFindByUser_ReturnsUserHistory() {
        // Arrange
        String username = "testuser";
        List<ChatHistory> mockHistories = Arrays.asList(mockChatHistory);
        
        when(repo.findByUsernameOrderByCreatedAtDesc(username)).thenReturn(mockHistories);

        // Act
        List<ChatHistory> result = chatHistoryService.findByUser(username);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockChatHistory.getId(), result.get(0).getId());
        
        verify(repo).findByUsernameOrderByCreatedAtDesc(username);
    }

    @Test
    void testFindByUser_EmptyResult_ReturnsEmptyList() {
        // Arrange
        String username = "nonexistentuser";
        
        when(repo.findByUsernameOrderByCreatedAtDesc(username)).thenReturn(new ArrayList<>());

        // Act
        List<ChatHistory> result = chatHistoryService.findByUser(username);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(repo).findByUsernameOrderByCreatedAtDesc(username);
    }

    @Test
    void testFindByUserAndTopic_ReturnsFilteredHistory() {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        List<ChatHistory> mockHistories = Arrays.asList(mockChatHistory);
        
        when(repo.findByUsernameAndTopicOrderByCreatedAtDesc(username, topic)).thenReturn(mockHistories);

        // Act
        List<ChatHistory> result = chatHistoryService.findByUserAndTopic(username, topic);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockChatHistory.getId(), result.get(0).getId());
        
        verify(repo).findByUsernameAndTopicOrderByCreatedAtDesc(username, topic);
    }

    @Test
    void testFindByUserAndTopic_EmptyResult_ReturnsEmptyList() {
        // Arrange
        String username = "testuser";
        String topic = "nonexistent topic";
        
        when(repo.findByUsernameAndTopicOrderByCreatedAtDesc(username, topic)).thenReturn(new ArrayList<>());

        // Act
        List<ChatHistory> result = chatHistoryService.findByUserAndTopic(username, topic);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(repo).findByUsernameAndTopicOrderByCreatedAtDesc(username, topic);
    }

    @Test
    void testFindByIdAndUsername_FoundAndMatches_ReturnsOptionalWithHistory() {
        // Arrange
        Long id = 1L;
        String username = "testuser";
        
        when(repo.findById(id)).thenReturn(Optional.of(mockChatHistory));

        // Act
        Optional<ChatHistory> result = chatHistoryService.findByIdAndUsername(id, username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockChatHistory.getId(), result.get().getId());
        
        verify(repo).findById(id);
    }

    @Test
    void testFindByIdAndUsername_FoundButDifferentUser_ReturnsEmpty() {
        // Arrange
        Long id = 1L;
        String username = "differentuser";
        
        when(repo.findById(id)).thenReturn(Optional.of(mockChatHistory));

        // Act
        Optional<ChatHistory> result = chatHistoryService.findByIdAndUsername(id, username);

        // Assert
        assertTrue(result.isEmpty());
        
        verify(repo).findById(id);
    }

    @Test
    void testFindByIdAndUsername_NotFound_ReturnsEmpty() {
        // Arrange
        Long id = 999L;
        String username = "testuser";
        
        when(repo.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<ChatHistory> result = chatHistoryService.findByIdAndUsername(id, username);

        // Assert
        assertTrue(result.isEmpty());
        
        verify(repo).findById(id);
    }

    @Test
    void testDeleteByIdIfOwned_ValidOwner_DeletesSuccessfully() {
        // Arrange
        Long id = 1L;
        String username = "testuser";
        
        when(repo.findById(id)).thenReturn(Optional.of(mockChatHistory));

        // Act
        chatHistoryService.deleteByIdIfOwned(id, username);

        // Assert
        verify(repo).findById(id);
        verify(repo).deleteById(id);
    }

    @Test
    void testDeleteByIdIfOwned_NotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Long id = 999L;
        String username = "testuser";
        
        when(repo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            chatHistoryService.deleteByIdIfOwned(id, username);
        });
        
        verify(repo).findById(id);
        verify(repo, never()).deleteById(any());
    }

    @Test
    void testDeleteByIdIfOwned_NotOwner_ThrowsSecurityException() {
        // Arrange
        Long id = 1L;
        String username = "differentuser";
        
        when(repo.findById(id)).thenReturn(Optional.of(mockChatHistory));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            chatHistoryService.deleteByIdIfOwned(id, username);
        });
        
        verify(repo).findById(id);
        verify(repo, never()).deleteById(any());
    }

    @Test
    void testFindByIdAndUsername_FoundAndMatching_ReturnsHistory() {
        // Arrange
        Long id = 1L;
        String username = "testuser";
        
        when(repo.findById(id)).thenReturn(Optional.of(mockChatHistory));

        // Act
        Optional<ChatHistory> result = chatHistoryService.findByIdAndUsername(id, username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockChatHistory.getId(), result.get().getId());
        assertEquals(mockChatHistory.getUsername(), result.get().getUsername());
        
        verify(repo).findById(id);
    }

    @Test
    void testFindByIdAndUsername_FoundButNotMatching_ReturnsEmpty() {
        // Arrange
        Long id = 1L;
        String username = "differentuser";
        
        when(repo.findById(id)).thenReturn(Optional.of(mockChatHistory));

        // Act
        Optional<ChatHistory> result = chatHistoryService.findByIdAndUsername(id, username);

        // Assert
        assertTrue(result.isEmpty());
        
        verify(repo).findById(id);
    }

    @Test
    void testSave_WithComplexArticlesObject_SerializesCorrectly() throws JsonProcessingException {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "What is trade policy?";
        String synthesizedAnswer = "Trade policy refers to...";
        
        Map<String, Object> complexArticle = new HashMap<>();
        complexArticle.put("title", "Test Article");
        complexArticle.put("url", "https://example.com");
        complexArticle.put("nested", Map.of("key", "value"));
        
        List<Object> complexArticles = Arrays.asList(complexArticle);
        
        String serializedMessages = "[{\"query\":\"What is trade policy?\",\"response\":\"Trade policy refers to...\",\"sources\":[{\"title\":\"Test Article\"}]}]";
        
        when(objectMapper.writeValueAsString(any())).thenReturn(serializedMessages);
        when(repo.save(any(ChatHistory.class))).thenReturn(mockChatHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, complexArticles, null);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatHistory.getId(), result.getId());
        
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(any(ChatHistory.class));
    }

    @Test
    void testSave_WithNullArticles_HandlesGracefully() throws JsonProcessingException {
        // Arrange
        String username = "testuser";
        String topic = "trade policy";
        String queryText = "What is trade policy?";
        String synthesizedAnswer = "Trade policy refers to...";
        
        String serializedMessages = "[{\"query\":\"What is trade policy?\",\"response\":\"Trade policy refers to...\",\"sources\":null}]";
        
        when(objectMapper.writeValueAsString(any())).thenReturn(serializedMessages);
        when(repo.save(any(ChatHistory.class))).thenReturn(mockChatHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(mockChatHistory.getId(), result.getId());
        
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(any(ChatHistory.class));
    }

    @Test
    void testSave_WithEmptyTopic_HandlesCorrectly() throws JsonProcessingException {
        // Arrange
        String username = "testuser";
        String topic = "";  // Empty topic
        String queryText = "What is trade policy?";
        String synthesizedAnswer = "Trade policy refers to...";
        
        String serializedMessages = "[{\"query\":\"What is trade policy?\",\"response\":\"Trade policy refers to...\",\"sources\":[]}]";
        
        when(objectMapper.writeValueAsString(any())).thenReturn(serializedMessages);
        when(repo.save(any(ChatHistory.class))).thenReturn(mockChatHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, new ArrayList<>(), null);

        // Assert
        assertNotNull(result);
        
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(any(ChatHistory.class));
    }

    @Test
    void testSave_WithWhitespaceOnlyTopic_HandlesCorrectly() throws JsonProcessingException {
        // Arrange
        String username = "testuser";
        String topic = "   ";  // Whitespace only topic
        String queryText = "What is trade policy?";
        String synthesizedAnswer = "Trade policy refers to...";
        
        String serializedMessages = "[{\"query\":\"What is trade policy?\",\"response\":\"Trade policy refers to...\",\"sources\":[]}]";
        
        when(objectMapper.writeValueAsString(any())).thenReturn(serializedMessages);
        when(repo.save(any(ChatHistory.class))).thenReturn(mockChatHistory);

        // Act
        ChatHistory result = chatHistoryService.save(username, topic, queryText, synthesizedAnswer, new ArrayList<>(), null);

        // Assert
        assertNotNull(result);
        
        verify(objectMapper).writeValueAsString(any());
        verify(repo).save(any(ChatHistory.class));
    }

    // Helper method to create mock article
    private Object createMockArticle(String title, String url) {
        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("url", url);
        return article;
    }
}