package com.techindna.eventsyncapi.controller.sessions;

import com.techindna.eventsyncapi.controller.SessionController;
import com.techindna.eventsyncapi.dto.auth.ParticipantRefDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.dto.session.EventRefDto;
import com.techindna.eventsyncapi.dto.session.RoomRefDto;
import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SpeakerRefDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.TooManyRequestException;
import com.techindna.eventsyncapi.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetSessionControllerTest {

    private final MockMvc mockMvc;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID EVENT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID SPEAKER_ID = UUID.fromString("d4e5f6a7-b8c9-0123-def0-234567890123");
    private static final UUID QUESTION_ID = UUID.fromString("e5f6a7b8-c9d0-1234-ef01-345678901234");
    private static final String SESSION_TITLE = "Introduction to Spring Boot";
    private static final String MOCK_IP = "127.0.0.1";

    GetSessionControllerTest() {
        sessionService = mock(SessionService.class);
        var controller = new SessionController(sessionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    private SessionDetailResponseDto buildDetail(boolean withSpeakersAndQuestions) {
        List<SpeakerRefDto> speakers = withSpeakersAndQuestions
                ? List.of(SpeakerRefDto.builder()
                .id(SPEAKER_ID)
                .firstName("Alice")
                .lastName("Dupont")
                .build())
                : null;

        List<QuestionResponseDto> questions = withSpeakersAndQuestions
                ? List.of(QuestionResponseDto.builder()
                .id(QUESTION_ID)
                .title("Dependency Injection?")
                .content("How does Spring resolve circular dependencies?")
                .anonymous(false)
                .upvotes(3)
                .createdAt(Instant.parse("2026-07-01T10:05:00Z"))
                .participant(ParticipantRefDto.builder()
                        .id(UUID.fromString("ebcfde24-726e-4bb4-8b55-786781d8b6bc"))
                        .firstName("Bob")
                        .lastName("Martin")
                        .email("bob@example.com")
                        .build())
                .build())
                : null;

        return SessionDetailResponseDto.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .description("A beginner-friendly session on Spring Boot fundamentals.")
                .startDate(Instant.parse("2026-07-01T10:00:00Z"))
                .endDate(Instant.parse("2026-07-01T12:00:00Z"))
                .room(RoomRefDto.builder().id(ROOM_ID).name("Salle Principale").build())
                .capacity(50)
                .event(EventRefDto.builder().id(EVENT_ID).title("DevCon 2026").build())
                .speakers(speakers)
                .questions(questions)
                .live(false)
                .build();
    }

    @Test
    @DisplayName("GET /sessions/{id} returns 200 with full session detail including speakers and questions")
    void getSessionById_withExistingId_returns200() throws Exception {
        when(sessionService.getSessionById(eq(SESSION_ID), any())).thenReturn(buildDetail(true));

        mockMvc.perform(get("/sessions/{id}", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.title").value(SESSION_TITLE))
                .andExpect(jsonPath("$.description").value("A beginner-friendly session on Spring Boot fundamentals."))
                .andExpect(jsonPath("$.startDate").value("2026-07-01T10:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2026-07-01T12:00:00Z"))
                .andExpect(jsonPath("$.room.id").value(ROOM_ID.toString()))
                .andExpect(jsonPath("$.room.name").value("Salle Principale"))
                .andExpect(jsonPath("$.capacity").value(50))
                .andExpect(jsonPath("$.event.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.event.title").value("DevCon 2026"))
                .andExpect(jsonPath("$.isLive").value(false))
                .andExpect(jsonPath("$.speakers[0].id").value(SPEAKER_ID.toString()))
                .andExpect(jsonPath("$.speakers[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.speakers[0].lastName").value("Dupont"))
                .andExpect(jsonPath("$.questions[0].id").value(QUESTION_ID.toString()))
                .andExpect(jsonPath("$.questions[0].title").value("Dependency Injection?"))
                .andExpect(jsonPath("$.questions[0].upvotes").value(3))
                .andExpect(jsonPath("$.questions[0].isAnonymous").value(false))
                .andExpect(jsonPath("$.questions[0].participant.firstName").value("Bob"));

        verify(sessionService).getSessionById(eq(SESSION_ID), any());
    }

    @Test
    @DisplayName("GET /sessions/{id} with no speakers and no questions returns 200 with null arrays")
    void getSessionById_withNoSpeakersOrQuestions_returns200() throws Exception {
        when(sessionService.getSessionById(eq(SESSION_ID), any())).thenReturn(buildDetail(false));

        mockMvc.perform(get("/sessions/{id}", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.speakers").doesNotExist())
                .andExpect(jsonPath("$.questions").doesNotExist());
    }

    @Test
    @DisplayName("GET /sessions/{id} when session not found returns 404")
    void getSessionById_whenSessionNotFound_returns404() throws Exception {
        when(sessionService.getSessionById(eq(SESSION_ID), any()))
                .thenThrow(new NotFoundException("Session " + SESSION_ID + " not found."));

        mockMvc.perform(get("/sessions/{id}", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Session " + SESSION_ID + " not found."));
    }

    @Test
    @DisplayName("GET /sessions/{id} when IP is blacklisted returns 429")
    void getSessionById_whenBlacklistedIp_returns429() throws Exception {
        when(sessionService.getSessionById(eq(SESSION_ID), any()))
                .thenThrow(new TooManyRequestException("Too many requests. Please try again later."));

        mockMvc.perform(get("/sessions/{id}", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }

    @Test
    @DisplayName("GET /sessions/{id} with invalid UUID format returns 400")
    void getSessionById_withInvalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/sessions/{id}", "not-a-uuid")
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /sessions/{id} with UUID all zeros returns 404")
    void getSessionById_withAllZerosUuid_returns404() throws Exception {
        UUID zero = new UUID(0L, 0L);
        when(sessionService.getSessionById(eq(zero), any()))
                .thenThrow(new NotFoundException("Session " + zero + " not found."));

        mockMvc.perform(get("/sessions/{id}", zero)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /sessions/{id} when session is live sets isLive true")
    void getSessionById_whenLive_returnsIsLiveTrue() throws Exception {
        var detail = SessionDetailResponseDto.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .description("desc")
                .startDate(Instant.parse("2026-07-01T10:00:00Z"))
                .endDate(Instant.parse("2026-07-01T12:00:00Z"))
                .room(RoomRefDto.builder().id(ROOM_ID).name("Salle Principale").build())
                .capacity(50)
                .event(EventRefDto.builder().id(EVENT_ID).title("DevCon 2026").build())
                .speakers(null)
                .questions(null)
                .live(true)
                .build();

        when(sessionService.getSessionById(eq(SESSION_ID), any())).thenReturn(detail);

        mockMvc.perform(get("/sessions/{id}", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLive").value(true));
    }

    @Test
    @DisplayName("GET /sessions/{id} forwards IP address to service")
    void getSessionById_forwardsIpAddressToService() throws Exception {
        when(sessionService.getSessionById(eq(SESSION_ID), eq(MOCK_IP))).thenReturn(buildDetail(true));

        mockMvc.perform(get("/sessions/{id}", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sessionService).getSessionById(SESSION_ID, MOCK_IP);
    }
}
