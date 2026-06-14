package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.room.RoomInputDto;
import com.techindna.eventsyncapi.dto.room.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomMcpTools {

    private final RoomService roomService;

    @Tool(description = "Create a new room with the given name. Returns the created room id and name.")
    public String createRoom(
            @ToolParam(description = "Name of the room to create (max 50 characters)") String name
    ) {
        RoomInputDto input = RoomInputDto.builder().name(name.strip()).build();
        try {
            RoomResponseDto room = roomService.createRoom(input);
            return "Room id" + room.getId() + ", name: " + room.getName() + " created.";
        } catch (Exception e) {
            return "Failed to create room: " + e.getMessage();
        }
    }

    @Tool(description = "List rooms with optional search filter and pagination. Returns room names and IDs.")
    public String listRooms(
            @ToolParam(description = "Optional search term to filter rooms by name") String search,
            @ToolParam(description = "Page number (default: 1)") int page,
            @ToolParam(description = "Items per page (default: 10)") int size
    ) {
        try {
            String ip = "127.0.0.1";
            RoomListResponseDto result = roomService.getAllRooms(page, size, search, ip);
            if (result.getData().isEmpty()) {
                return "No rooms found.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Rooms (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var room : result.getData()) {
                sb.append("- ").append(room.getName()).append(" (").append(room.getId()).append(")\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Failed to list rooms: " + e.getMessage();
        }
    }

    @Tool(description = "Get details of a specific room by its UUID.")
    public String getRoom(
            @ToolParam(description = "UUID of the room") String id
    ) {
        try {
            RoomResponseDto room = roomService.getRoomById(UUID.fromString(id), "127.0.0.1");
            return "Room details:\n- ID: " + room.getId() + "\n- Name: " + room.getName();
        } catch (Exception e) {
            return "Failed to get room: " + e.getMessage();
        }
    }

    @Tool(description = "Update the name of an existing room. Provide the room UUID and a new name.")
    public String updateRoom(
            @ToolParam(description = "UUID of the room to update") String id,
            @ToolParam(description = "New name for the room (max 50 characters)") String name
    ) {
        try {
            RoomInputDto input = RoomInputDto.builder().name(name.strip()).build();
            RoomResponseDto room = roomService.updateRoom(UUID.fromString(id), input);
            return "Room id: " + room.getId() + "name: " + room.getName() + " updated.";
        } catch (Exception e) {
            return "Failed to update room: " + e.getMessage();
        }
    }

    @Tool(description = "Delete a room by its UUID.")
    public String deleteRoom(
            @ToolParam(description = "UUID of the room to delete") String id
    ) {
        try {
            roomService.deleteRoom(UUID.fromString(id));
            return "Room " + id + " deleted.";
        } catch (Exception e) {
            return "Failed to delete room: " + e.getMessage();
        }
    }
}
