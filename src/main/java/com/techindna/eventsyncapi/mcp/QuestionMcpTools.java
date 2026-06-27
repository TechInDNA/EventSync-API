package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.dto.question.QuestionRequestDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.dto.question.UpvoteResponseDto;
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
    private static final UUID ADMIN_USER_ID = UUID.fromString("3aad95aa-0a42-451f-b300-3a170fd0211d");
    private static final Logger LOG = logger(QuestionMcpTools.class);

    private final QuestionService questionService;

    @Tool(name = "createQuestion",
            description = """
                    Create a new question on a session.

                    Returns a confirmation including the question id, title, content,
                    author, anonymous flag, and upvote count (always 0 on creation).

                    Validation rules — strict pass or call fails with 422:
                    • title: required, non-blank, max 50 chars, must start and end with a
                      letter or digit. Allowed characters are a-z A-Z 0-9 and space only.
                      No punctuation, no leading/trailing spaces.
                    • content: required, non-blank, max 1000 chars. Allowed characters are
                      A-Za-z0-9 and . , ; " ! ' - and space.
                    • isAnonymous: optional boolean. Defaults to false (question shows the
                      author). Pass true to hide the participant ref.

                    404 if the session UUID does not exist.
                    """)
    public String createQuestion(
            @ToolParam(description = "UUID of the session to post the question on") String sessionId,
            @ToolParam(description = "Title of the question (max 50 characters, no punctuation)") String title,
            @ToolParam(description = "Body/content of the question (max 1000 characters)") String content,
            @ToolParam(description = "Set true to hide the participant identity, defaults to false") Boolean isAnonymous
    ) {
        return run(LOG, "createQuestion", () -> {
            UUID sId = parseUuid(sessionId);
            QuestionRequestDto input = QuestionRequestDto.builder()
                    .title(title.strip())
                    .content(content.strip())
                    .anonymous(isAnonymous != null ? isAnonymous : false)
                    .build();
            QuestionResponseDto question = questionService.createQuestion(sId, input, ADMIN_USER_ID);
            return formatQuestionResponse("Question created:", question);
        });
    }

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

    @Tool(name = "upvoteQuestion",
            description = """
                    Toggle an upvote on a question. If the admin has already upvoted,
                    this removes the upvote; otherwise it adds one.

                    Returns the new total upvote count for the question.

                    404 if the question UUID does not exist.
                    """)
    public String upvoteQuestion(
            @ToolParam(description = "UUID of the session that owns the question") String sessionId,
            @ToolParam(description = "UUID of the question to upvote") String questionId
    ) {
        return run(LOG, "upvoteQuestion", () -> {
            UUID sId = parseUuid(sessionId);
            UUID qId = parseUuid(questionId);
            UpvoteResponseDto result = questionService.upvoteQuestion(qId, sId, ADMIN_USER_ID);
            return String.format("Question %s now has %d upvote(s).", qId, result.getUpvoteCount());
        });
    }

    private static String formatQuestionResponse(String header, QuestionResponseDto question) {
        var sb = new StringBuilder(String.format("""
                        %s
                        - ID: %s
                        - Title: %s
                        - Content: %s
                        - Created at: %s
                        - Upvotes: %d
                        - Anonymous: %s
                        """,
                header,
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpvotes(),
                question.isAnonymous()));
        if (question.isAnonymous() || question.getParticipant() == null) {
            sb.append("- Author: hidden\n");
        } else {
            sb.append("- Author: ")
                    .append(question.getParticipant().getFirstName())
                    .append(" ")
                    .append(question.getParticipant().getLastName())
                    .append(" (").append(question.getParticipant().getId()).append(")\n");
        }
        return sb.toString();
    }
}