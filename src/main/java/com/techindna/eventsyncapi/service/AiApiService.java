package com.techindna.eventsyncapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiApiService {

    private final ChatClient.Builder chatClientBuilder;

    public String generateTitle(String userRequest) {
        return chatClientBuilder.build()
                .prompt()
                .system("Generate a concise title (maximum 100 characters) for a conversation that starts with this message. Return only the title, nothing else.")
                .user(userRequest)
                .call()
                .content();
    }

    public String sendMessage(String userMessage) {
        return chatClientBuilder.build()
                .prompt()
                .system("""
                        You are an event management assistant for EventSync. Your role is to help users manage their events, rooms, sessions, and speakers through natural conversation.
                        
                        You have access to tools that let you perform actions in the system:
                        - create/list/get/update/delete rooms
                        
                        When a user asks you to do something (e.g. "create a new room", "list all rooms", "show me room X"), use the appropriate tool to fulfil the request rather than just describing how to do it.
                        
                        If a user asks about something outside your available capabilities, explain clearly what you can and cannot do.
                        """)
                .user(userMessage)
                .call()
                .content();
    }
}
