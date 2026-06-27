package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.room.RoomInputDto;
import com.techindna.eventsyncapi.dto.room.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.techindna.eventsyncapi.mcp.McpToolSupport.logger;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.pageOrDefault;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.parseUuid;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.run;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.sizeOrDefault;

@Component
@RequiredArgsConstructor
public class RoomMcpTools {

    private static final String IP = "127.0.0.1";
    private static final Logger LOG = logger(RoomMcpTools.class);

    private final RoomService roomService;

    @Tool(name = "createRoom",
            description = """
                    Create a new room with the given name.

                    Returns a confirmation including the created room id and name.

                    Validation rules — strict pass or call fails with 422:
                    • name: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces.

                    Conflicts: duplicate name → 409 "Room ... already exists.".
                    """)
    public String createRoom(
            @ToolParam(description = "Name of the room to create (max 50 characters)") String name
    ) {
        return run(LOG, "createRoom", () -> {
            RoomInputDto input = RoomInputDto.builder().name(name.strip()).build();
            RoomResponseDto room = roomService.createRoom(input);
            return String.format("Room created:%n- ID: %s%n- Name: %s", room.getId(), room.getName());
        });
    }

    @Tool(name = "listRooms",
            description = """
                    List rooms with optional search filter and pagination.

                    Returns a list of room names and ids, or 'No rooms found.' if empty.

                    Validation rules — strict pass or call fails with 422:
                    • search: must contain only letters, digits, spaces, hyphens,
                      and apostrophes. Avoid commas, slashes, colons, or other punctuation.
                    """)
    public String listRooms(
            @ToolParam(description = "Optional search term to filter rooms by name") String search,
            @ToolParam(description = "Page number (optional, defaults to 1)") Integer page,
            @ToolParam(description = "Items per page (optional, defaults to 10)") Integer size
    ) {
        return run(LOG, "listRooms", () -> {
            RoomListResponseDto result = roomService.getAllRooms(
                    pageOrDefault(page), sizeOrDefault(size), search, IP);
            if (result.getData() == null || result.getData().isEmpty()) {
                return "No rooms found.";
            }
            var sb = new StringBuilder();
            sb.append("Rooms (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var room : result.getData()) {
                sb.append("- ").append(room.getName())
                        .append(" (").append(room.getId()).append(")\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "getRoom",
            description = """
                    Get details of a specific room by its UUID.

                    Returns the room id and name.
                    """)
    public String getRoom(
            @ToolParam(description = "UUID of the room") String id
    ) {
        return run(LOG, "getRoom", () -> {
            RoomResponseDto room = roomService.getRoomById(parseUuid(id), IP);
            return String.format("Room details:%n- ID: %s%n- Name: %s", room.getId(), room.getName());
        });
    }

    @Tool(name = "updateRoom",
            description = """
                    Update the name of an existing room.

                    Returns a confirmation including the room id and the new name.

                    Validation rules — strict pass or call fails with 422:
                    • name: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces.
                    """)
    public String updateRoom(
            @ToolParam(description = "UUID of the room to update") String id,
            @ToolParam(description = "New name for the room (max 50 characters)") String name
    ) {
        return run(LOG, "updateRoom", () -> {
            RoomInputDto input = RoomInputDto.builder().name(name.strip()).build();
            RoomResponseDto room = roomService.updateRoom(parseUuid(id), input);
            return String.format("Room updated:%n- ID: %s%n- Name: %s", room.getId(), room.getName());
        });
    }

    @Tool(name = "deleteRoom",
            description = """
                    Delete a room by its UUID.

                    Returns a confirmation string.
                    """)
    public String deleteRoom(
            @ToolParam(description = "UUID of the room to delete") String id
    ) {
        return run(LOG, "deleteRoom", () -> {
            UUID roomId = parseUuid(id);
            roomService.deleteRoom(roomId);
            return String.format("Room %s deleted.", roomId);
        });
    }
}
