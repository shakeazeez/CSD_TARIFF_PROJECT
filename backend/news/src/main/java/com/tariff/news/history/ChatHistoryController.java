package com.tariff.news.history;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Chat History", description = "Manage chatbot conversation history and retrieve past conversations")
@RestController
@RequestMapping("/news/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService service;
    private final ObjectMapper objectMapper;

    @Operation(
        summary = "Get chat history for a user",
        description = "Retrieve all conversation history for a specific user, with optional topic filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ConversationDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied - user mismatch")
    })
    @GetMapping("/{username}")
    public ResponseEntity<List<ConversationDto>> getHistoryForUser(
            @Parameter(description = "Username to get history for", required = true)
            @PathVariable String username,
            @Parameter(description = "Optional topic filter for conversations")
            @RequestParam(required = false) String topic,
            java.security.Principal principal) {
        // Use the authenticated principal's username rather than trusting the path variable
        String authUser = principal != null ? principal.getName() : username;
        if (!authUser.equals(username)) {
            // ignore path username and use authenticated user
            username = authUser;
        }
        List<ChatHistory> list = (topic == null || topic.isEmpty()) ? service.findByUser(username) : service.findByUserAndTopic(username, topic);
        List<ConversationDto> dto = list.stream().map(h -> toConversationDto(h, objectMapper)).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @Operation(
        summary = "Save chat history for a user",
        description = "Save a conversation to the user's chat history"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History saved successfully",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ConversationDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied - user mismatch"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/{username}")
    public ResponseEntity<ConversationDto> saveHistory(
            @Parameter(description = "Username to save history for", required = true)
            @PathVariable String username,
            @Parameter(description = "Conversation data to save", required = true)
            @RequestBody ConversationDto body,
            java.security.Principal principal) {
        String authUser = principal != null ? principal.getName() : username;
        if (!authUser.equals(username)) {
            username = authUser;
        }
        // For now, not used, since saving is done in process
        return ResponseEntity.ok(null);
    }

    @Operation(
        summary = "Delete chat history entry",
        description = "Delete a specific conversation from the user's chat history"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History entry deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access"),
        @ApiResponse(responseCode = "403", description = "Access denied - user mismatch or not owner"),
        @ApiResponse(responseCode = "404", description = "History entry not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{username}/{id}")
    public ResponseEntity<Void> deleteHistory(
            @Parameter(description = "Username who owns the history", required = true)
            @PathVariable String username,
            @Parameter(description = "Id of the history entry to delete", required = true)
            @PathVariable Long id) {
        try {
            // Check if the chat history exists and belongs to the user
            var chatHistoryOpt = service.findById(id);
            if (chatHistoryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            var chatHistory = chatHistoryOpt.get();
            // Check authorization - user can only delete their own history
            if (!username.equals(chatHistory.getUsername())) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            System.err.println("Delete failed - not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Delete failed - internal error: " + e.getMessage());
            e.printStackTrace();
            // Check if it's optimistic locking and the record is actually deleted
            if (e.getCause() instanceof org.hibernate.StaleObjectStateException && !service.existsById(id)) {
                System.err.println("Record was already deleted, returning success");
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    private static ConversationDto toConversationDto(ChatHistory h, ObjectMapper om) {
        ConversationDto d = new ConversationDto();
        d.setId(h.getId());
        d.setTitle(h.getTopic());
        d.setCreatedAt(h.getCreatedAt());
        try {
            List<Map<String, Object>> msgs = om.readValue(h.getMessages(), new TypeReference<List<Map<String, Object>>>(){});
            List<MessageDto> messageDtos = msgs.stream().map(m -> {
                MessageDto md = new MessageDto();
                md.setQuery((String) m.get("query"));
                md.setResponse((String) m.get("response"));
                md.setSources(m.get("sources"));
                return md;
            }).collect(Collectors.toList());
            d.setMessages(messageDtos);
        } catch (Exception e) {
            d.setMessages(List.of());
        }
        return d;
    }
}
