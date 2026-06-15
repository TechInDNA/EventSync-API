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
                .user(userMessage)
                .call()
                .content();
    }
}
