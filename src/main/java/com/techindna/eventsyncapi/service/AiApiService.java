package com.techindna.eventsyncapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiApiService {

    private final ChatClient.Builder chatClientBuilder;

    public String sendMessage(String userMessage) {
        return chatClientBuilder.build()
                .prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
