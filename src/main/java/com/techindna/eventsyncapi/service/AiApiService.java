package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.mcp.RoomMcpTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiApiService {

    private final ChatClient.Builder chatClientBuilder;
    private final RoomMcpTools roomMcpTools;

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
                        
                        Examples:
                        User: "create a new room called Workshop Hall A"
                        You call createRoom(name="Workshop Hall A") and tell the user the result.
                        
                        User: "list all rooms"
                        You call listRooms(search="", page=1, size=10) and present the results.
                        """)
                .user(userMessage)
                .call()
                .content();
    }
}
