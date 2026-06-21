package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.entity.Message.SenderType;
import com.techindna.eventsyncapi.mcp.RoomMcpTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiApiService {

    private final ChatClient.Builder chatClientBuilder;
    private final RoomMcpTools roomMcpTools;

    public Optional<String> generateTitle(String userRequest) {
        return Optional.ofNullable(chatClientBuilder.build()
                .prompt()
                .system("Generate a concise title (maximum 100 characters) for a conversation that starts with this message. Return only the title, nothing else.")
                .user(userRequest)
                .call()
                .content());
    }

    public String sendMessage(String userMessage, List<com.techindna.eventsyncapi.entity.Message> history) {
        var conversationMessages = new ArrayList<Message>();

        for (var msg : history) {
            if (msg.getSenderType() == SenderType.user) {
                conversationMessages.add(new UserMessage(msg.getContent()));
            } else {
                conversationMessages.add(new AssistantMessage(msg.getContent()));
            }
        }

        conversationMessages.add(new UserMessage(userMessage));

        return chatClientBuilder.build()
                .prompt()
                .tools(roomMcpTools)
                .system("""
                        You are an event management assistant for EventSync. Your role is to help users manage their events, rooms, sessions, and speakers through natural conversation.
                        
                        You have access to tools that let you perform actions in the system in real time. When a user asks you to do something, you MUST call the appropriate tool — do not just describe what you would do.
                        
                        Available tools:
                        - createRoom(name) — create a new room with the given name
                        - listRooms(search, page, size) — list rooms with optional search filter
                        - getRoom(id) — get details of a specific room by its UUID
                        - updateRoom(id, name) — update the name of a room
                        - deleteRoom(id) — delete a room by its UUID
                        """)
                .messages(conversationMessages)
                .call()
                .content();
    }
}
