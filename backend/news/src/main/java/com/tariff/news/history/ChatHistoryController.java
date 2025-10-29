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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/news/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService service;
    private final ObjectMapper objectMapper;

    @GetMapping("/{username}")
    public ResponseEntity<List<ConversationDto>> getHistoryForUser(@PathVariable String username,
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

    @PostMapping("/{username}")
    public ResponseEntity<ConversationDto> saveHistory(@PathVariable String username, @RequestBody ConversationDto body, java.security.Principal principal) {
        String authUser = principal != null ? principal.getName() : username;
        if (!authUser.equals(username)) {
            username = authUser;
        }
        // For now, not used, since saving is done in process
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{username}/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable String username, @PathVariable Long id, java.security.Principal principal) {
        try {
            String authUser = principal != null ? principal.getName() : username;
            if (!authUser.equals(username)) {
                username = authUser;
            }
            service.deleteByIdIfOwned(id, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
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
