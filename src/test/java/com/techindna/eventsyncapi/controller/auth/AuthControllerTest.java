package com.techindna.eventsyncapi.controller.auth;

import com.techindna.eventsyncapi.controller.AuthController;
import com.techindna.eventsyncapi.dto.auth.UserResponseDto;
import com.techindna.eventsyncapi.dto.auth.AuthLoginRequestDto;
import com.techindna.eventsyncapi.dto.auth.AuthLoginResponseDto;
import com.techindna.eventsyncapi.dto.auth.AuthParticipantRequestDto;
import com.techindna.eventsyncapi.dto.auth.AuthParticipantResponseDto;
import com.techindna.eventsyncapi.dto.auth.ParticipantRefDto;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.TooManyRequestException;
import com.techindna.eventsyncapi.exception.UnauthorizedException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    private static final UUID ADMIN_ID = UUID.fromString("3f553f56-792c-4c80-9ea9-b259ef1247a9");
    private static final UUID PARTICIPANT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String VALID_TOKEN = "eyJhbG...test";

    AuthControllerTest() {
        authService = mock(AuthService.class);
        objectMapper = new ObjectMapper();
        var controller = new AuthController(authService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        var request = validRequest();
        var userDto = UserResponseDto.builder()
                .id(ADMIN_ID)
                .firstName("Admin")
                .lastName("User")
                .email("admin@eventsync.com")
                .role(Role.ADMIN)
                .build();
        var response = AuthLoginResponseDto.builder()
                .token(VALID_TOKEN)
                .user(userDto)
                .build();

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(VALID_TOKEN))
                .andExpect(jsonPath("$.user.id").value(ADMIN_ID.toString()))
                .andExpect(jsonPath("$.user.email").value("admin@eventsync.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void login_withValidCredentials_setsHttpOnlyCookie() throws Exception {
        var request = validRequest();
        var userDto = UserResponseDto.builder()
                .id(ADMIN_ID)
                .firstName("Admin")
                .lastName("User")
                .email("admin@eventsync.com")
                .role(Role.ADMIN)
                .build();
        var response = AuthLoginResponseDto.builder()
                .token(VALID_TOKEN)
                .user(userDto)
                .build();

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().secure("jwt", true));
    }

    @Test
    void login_withEmptyBody_returns422() throws Exception {
        var emptyRequest = new AuthLoginRequestDto();
        when(authService.login(eq(emptyRequest), any(), any()))
                .thenThrow(new UnprocessableEntityException("The field email is required and cannot be blank."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    void login_withBlankEmail_returns422() throws Exception {
        var request = new AuthLoginRequestDto();
        request.setEmail("");
        request.setPassword("password123");

        when(authService.login(eq(request), any(), any()))
                .thenThrow(new UnprocessableEntityException("The field email is required and cannot be blank."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void login_withInvalidEmail_returns422() throws Exception {
        var request = new AuthLoginRequestDto();
        request.setEmail("not-an-email");
        request.setPassword("password123");

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenThrow(new UnprocessableEntityException("Invalid email format: 'not-an-email'"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void login_withBlankPassword_returns422() throws Exception {
        var request = new AuthLoginRequestDto();
        request.setEmail("admin@eventsync.com");
        request.setPassword("");

        when(authService.login(eq(request), any(), any()))
                .thenThrow(new UnprocessableEntityException("The field password is required and cannot be blank."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void login_withUnknownEmail_returns401() throws Exception {
        var request = validRequest();

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        var request = validRequest();
        request.setPassword("wrongpassword");

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBlockedIp_returns401() throws Exception {
        var request = validRequest();

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenThrow(new UnauthorizedException(
                        "You are not authorized to access this resource due to malicious behavior."
                ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withTooManyAttempts_returns429() throws Exception {
        var request = validRequest();

        when(authService.login(any(AuthLoginRequestDto.class), any(), any()))
                .thenThrow(new TooManyRequestException(
                        "You are blocked due to too many failed login attempts."
                ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void participate_withValidData_returns200AndToken() throws Exception {
        var request = validParticipantRequest();
        var participantRef = ParticipantRefDto.builder()
                .id(PARTICIPANT_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        var response = AuthParticipantResponseDto.builder()
                .token(VALID_TOKEN)
                .participant(participantRef)
                .build();

        when(authService.participate(any(AuthParticipantRequestDto.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(VALID_TOKEN))
                .andExpect(jsonPath("$.participant.id").value(PARTICIPANT_ID.toString()))
                .andExpect(jsonPath("$.participant.firstName").value("John"))
                .andExpect(jsonPath("$.participant.lastName").value("Doe"))
                .andExpect(jsonPath("$.participant.email").value("john.doe@example.com"));
    }

    @Test
    void participate_withBlockedIp_returns401() throws Exception {
        var request = validParticipantRequest();

        when(authService.participate(any(AuthParticipantRequestDto.class), any()))
                .thenThrow(new UnauthorizedException(
                        "You are not authorized to access this resource due to malicious behavior."
                ));

        mockMvc.perform(post("/auth/participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void participate_withEmptyBody_returns422() throws Exception {
        var emptyRequest = new AuthParticipantRequestDto();
        when(authService.participate(eq(emptyRequest), any()))
                .thenThrow(new UnprocessableEntityException("The field firstName is required and cannot be blank."));

        mockMvc.perform(post("/auth/participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    void participate_withBlankFields_returns422() throws Exception {
        var request = new AuthParticipantRequestDto();
        request.setFirstName("");
        request.setLastName("");
        request.setEmail("");

        when(authService.participate(eq(request), any()))
                .thenThrow(new UnprocessableEntityException("The field firstName is required and cannot be blank."));

        mockMvc.perform(post("/auth/participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void participate_withInvalidEmail_returns422() throws Exception {
        var request = new AuthParticipantRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("not-an-email");

        when(authService.participate(any(AuthParticipantRequestDto.class), any()))
                .thenThrow(new UnprocessableEntityException("Invalid email format: 'not-an-email'"));

        mockMvc.perform(post("/auth/participant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    private AuthLoginRequestDto validRequest() {
        var dto = new AuthLoginRequestDto();
        dto.setEmail("admin@eventsync.com");
        dto.setPassword("admin123");
        return dto;
    }

    private AuthParticipantRequestDto validParticipantRequest() {
        var dto = new AuthParticipantRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        return dto;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
