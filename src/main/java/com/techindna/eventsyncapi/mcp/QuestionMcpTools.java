package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.service.QuestionService;
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
public class QuestionMcpTools {

    private static final String IP = "127.0.0.1";
    private static final Logger LOG = logger(QuestionMcpTools.class);

    private final QuestionService questionService;

    @Tool(name = "findQuestions",
            description = """
                    Find/list questions posted on a session with optional title filter and pagination.

                    Returns a list of question titles, ids, authors (hidden if anonymous),
                    upvote counts, and createdAt timestamps, or 'No questions found.' if empty.

                    Validation rules — strict pass or call fails with 422:
                    • title filter: must contain only letters, digits, spaces, hyphens,
                      and apostrophes. Avoid commas, slashes, colons, or other punctuation.
                    • sort: 'upvotes' (default, descending by count) or 'createdAt'
                      (ascending by creation time). Other values fall back to
                      newest-first ordering.

                    404 if the session UUID does not exist.
                    """)
    public String findQuestions(
            @ToolParam(description = "UUID of the session to list questions from") String sessionId,
            @ToolParam(description = "Optional filter on the question title (case-insensitive partial match)") String title,
            @ToolParam(description = "Sort order: 'upvotes' (default) or 'createdAt'") String sort,
            @ToolParam(description = "Page number (optional, defaults to 1)") Integer page,
            @ToolParam(description = "Items per page (optional, defaults to 20)") Integer size
    ) {
        return run(LOG, "findQuestions", () -> {
            UUID sId = parseUuid(sessionId);
            String sortKey = (sort == null || sort.isBlank()) ? "upvotes" : sort.strip();
            QuestionListResponseDto result = questionService.getQuestionsBySessionId(
                    sId,
                    pageOrDefault(page),
                    sizeOrDefault(size),
                    sortKey,
                    title,
                    IP);
            if (result.getData() == null || result.getData().isEmpty()) {
                return "No questions found.";
            }
            var sb = new StringBuilder();
            sb.append("Questions (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var question : result.getData()) {
                sb.append("- ").append(question.getTitle())
                        .append(" (").append(question.getId()).append(")")
                        .append(" — ").append(question.getUpvotes()).append(" upvotes")
                        .append(", created ").append(question.getCreatedAt());
                if (question.isAnonymous()) {
                    sb.append(", anonymous");
                } else if (question.getParticipant() != null) {
                    sb.append(", by ").append(question.getParticipant().getFirstName())
                            .append(" ").append(question.getParticipant().getLastName());
                }
                sb.append("\n");
            }
            return sb.toString();
        });
    }
}