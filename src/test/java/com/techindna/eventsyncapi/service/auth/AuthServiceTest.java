package com.techindna.eventsyncapi.service.auth;

import com.techindna.eventsyncapi.dto.auth.AuthLoginRequestDto;
import com.techindna.eventsyncapi.dto.auth.AuthLoginResponseDto;
import com.techindna.eventsyncapi.dto.auth.AuthParticipantRequestDto;
import com.techindna.eventsyncapi.dto.auth.AuthParticipantResponseDto;
import com.techindna.eventsyncapi.entity.BlacklistedIp;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.TooManyRequestException;
import com.techindna.eventsyncapi.exception.UnauthorizedException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.UserMapper;
import com.techindna.eventsyncapi.repository.BlacklistedIpRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.config.TokenProvider;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository;
    private final BlacklistedIpRepository blacklistedIpRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DataValidator dataValidator;
    private final AuthService authService;

    private static final UUID ADMIN_ID = UUID.fromString("3f553f56-792c-4c80-9ea9-b259ef1247a9");
    private static final String VALID_TOKEN = "eyJhbG...test";
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_UA = "TestAgent/1.0";

    private User adminUser;
    private AuthLoginRequestDto validRequest;

    AuthServiceTest() {
        this.userRepository = mock(UserRepository.class);
        this.blacklistedIpRepository = mock(BlacklistedIpRepository.class);
        this.tokenProvider = mock(TokenProvider.class);
        this.passwordEncoder = mock(PasswordEncoder.class);
        this.userMapper = new UserMapper();
        this.dataValidator = new DataValidator();
        this.authService = new AuthService(
                dataValidator,
                userRepository,
                blacklistedIpRepository,
                tokenProvider,
                passwordEncoder,
                userMapper
        );
    }

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(ADMIN_ID)
                .firstName("Admin")
                .lastName("User")
                .email("admin@eventsync.com")
                .password("$argon2id$v=19$m=16384,t=2,p=1$hash")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        validRequest = new AuthLoginRequestDto();
        validRequest.setEmail("admin@eventsync.com");
        validRequest.setPassword("admin123");
    }

    @Nested
    @DisplayName("Successful login")
    class SuccessfulLogin {

        @Test
        @DisplayName("returns token and user for valid admin credentials")
        void validAdminCredentials_returnsAuthResponse() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(true);
            when(tokenProvider.generateAccessToken(adminUser))
                    .thenReturn(VALID_TOKEN);

            AuthLoginResponseDto response = authService.login(validRequest, TEST_IP, TEST_UA);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getId()).isEqualTo(ADMIN_ID);
            assertThat(response.getUser().getEmail()).isEqualTo("admin@eventsync.com");
            assertThat(response.getUser().getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("clears blacklist entry after successful login")
        void successfulLogin_clearsBlacklist() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(true);
            when(tokenProvider.generateAccessToken(adminUser))
                    .thenReturn(VALID_TOKEN);

            authService.login(validRequest, TEST_IP, TEST_UA);

            verify(blacklistedIpRepository).deleteByIpAddress(TEST_IP);
        }
    }

    @Nested
    @DisplayName("Authentication failures")
    class AuthenticationFailures {

        @Test
        @DisplayName("throws UnauthorizedException when email does not exist")
        void nonExistentEmail_throwsUnauthorized() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("throws UnauthorizedException when user is not an ADMIN")
        void nonAdminRole_throwsUnauthorized() {
            User speaker = User.builder()
                    .id(UUID.randomUUID())
                    .firstName("Speaker")
                    .lastName("User")
                    .email("speaker@eventsync.com")
                    .password("hash")
                    .role(Role.SPEAKER)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(speaker));

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when email format is invalid")
        void invalidEmailFormat_throwsUnprocessableEntity() {
            validRequest.setEmail("not-an-email");

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when email is null")
        void nullEmail_throwsUnprocessableEntity() {
            validRequest.setEmail(null);

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("The field email is required and cannot be blank.");
        }

        @Test
        @DisplayName("throws UnauthorizedException when password does not match")
        void wrongPassword_throwsUnauthorized() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(false);

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("Failed attempt tracking")
    class FailedAttemptTracking {

        @Test
        @DisplayName("creates new blacklist entry on first failure")
        void firstFailure_createsBlacklistEntry() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(false);

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class);

            ArgumentCaptor<BlacklistedIp> captor = ArgumentCaptor.forClass(BlacklistedIp.class);
            verify(blacklistedIpRepository).save(captor.capture());

            BlacklistedIp saved = captor.getValue();
            assertThat(saved.getIpAddress()).isEqualTo(TEST_IP);
            assertThat(saved.getUserAgent()).isEqualTo(TEST_UA);
            assertThat(saved.getFailedAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("increments failed attempts on subsequent failures")
        void subsequentFailures_incrementAttempts() {
            BlacklistedIp existing = BlacklistedIp.builder()
                    .id(UUID.randomUUID())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_UA)
                    .failedAttempts(2)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(false);
            when(blacklistedIpRepository.findByIpAddress(TEST_IP))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class);

            assertThat(existing.getFailedAttempts()).isEqualTo(3);
            verify(blacklistedIpRepository).save(existing);
        }

        @Test
        @DisplayName("throws UnauthorizedException when IP already at max attempts")
        void maxAttemptsReached_throwsTooManyRequests() {
            BlacklistedIp blocked = BlacklistedIp.builder()
                    .id(UUID.randomUUID())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_UA)
                    .failedAttempts(5)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(blacklistedIpRepository.findByIpAddress(TEST_IP))
                    .thenReturn(Optional.of(blocked));

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("malicious behavior");
        }

        @Test
        @DisplayName("shows remaining attempts count in error message")
        void failureMessage_showsRemainingAttempts() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(false);

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("4 attempt(s) left");
        }
    }

    @Nested
    @DisplayName("Rate limiting")
    class RateLimiting {

        @Test
        @DisplayName("throws TooManyRequestException when blacklist reaches limit")
        void fifthAttempt_throwsTooManyRequests() {
            when(userRepository.findByEmail(validRequest.getEmail()))
                    .thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches(validRequest.getPassword(), adminUser.getPassword()))
                    .thenReturn(false);

            BlacklistedIp existing = BlacklistedIp.builder()
                    .id(UUID.randomUUID())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_UA)
                    .failedAttempts(4)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(blacklistedIpRepository.findByIpAddress(TEST_IP))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(TooManyRequestException.class)
                    .hasMessageContaining("blocked due to too many failed login attempts");
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("does not increment failed attempts when IP is already blocked at limit")
        void alreadyBlocked_doesNotIncrement() {
            BlacklistedIp blocked = BlacklistedIp.builder()
                    .id(UUID.randomUUID())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_UA)
                    .failedAttempts(5)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(blacklistedIpRepository.findByIpAddress(TEST_IP))
                    .thenReturn(Optional.of(blocked));

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class);

            assertThat(blocked.getFailedAttempts()).isEqualTo(5);
            verify(blacklistedIpRepository, never()).save(any());
        }

        @Test
        @DisplayName("still blocks when IP has more than 5 attempts")
        void excessAttempts_stillBlocked() {
            BlacklistedIp blocked = BlacklistedIp.builder()
                    .id(UUID.randomUUID())
                    .ipAddress(TEST_IP)
                    .userAgent(TEST_UA)
                    .failedAttempts(10)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(blacklistedIpRepository.findByIpAddress(TEST_IP))
                    .thenReturn(Optional.of(blocked));

            assertThatThrownBy(() -> authService.login(validRequest, TEST_IP, TEST_UA))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("malicious behavior");
        }
    }

    @Nested
    @DisplayName("Participant registration / identification")
    class ParticipantRegistration {

        private static final UUID PARTICIPANT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        private static final String PARTICIPANT_TOKEN = "eyJhbG...participant-token";

        private AuthParticipantRequestDto validParticipantRequest;

        @BeforeEach
        void setUp() {
            validParticipantRequest = new AuthParticipantRequestDto();
            validParticipantRequest.setFirstName("John");
            validParticipantRequest.setLastName("Doe");
            validParticipantRequest.setEmail("john.doe@example.com");
        }

        @Test
        @DisplayName("creates new participant when email does not exist")
        void newEmail_createsParticipantAndReturnsToken() {
            User createdUser = User.builder()
                    .id(PARTICIPANT_ID)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .role(Role.PARTICIPANT)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmailAndNames(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(userRepository.insertParticipant(anyString(), anyString(), anyString()))
                    .thenReturn(createdUser);
            when(tokenProvider.generateAccessToken(any(User.class)))
                    .thenReturn(PARTICIPANT_TOKEN);

            AuthParticipantResponseDto response = authService.participate(validParticipantRequest, TEST_IP);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(PARTICIPANT_TOKEN);
            assertThat(response.getParticipant()).isNotNull();
            assertThat(response.getParticipant().getId()).isEqualTo(PARTICIPANT_ID);
            assertThat(response.getParticipant().getFirstName()).isEqualTo("John");
            assertThat(response.getParticipant().getLastName()).isEqualTo("Doe");
            assertThat(response.getParticipant().getEmail()).isEqualTo("john.doe@example.com");

            verify(userRepository).findByEmailAndNames(anyString(), anyString(), anyString());
            verify(userRepository).insertParticipant(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("returns existing participant when email already registered")
        void existingParticipant_returnsToken() {
            User existing = User.builder()
                    .id(PARTICIPANT_ID)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .role(Role.PARTICIPANT)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmailAndNames(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.of(existing));
            when(tokenProvider.generateAccessToken(any(User.class)))
                    .thenReturn(PARTICIPANT_TOKEN);

            AuthParticipantResponseDto response = authService.participate(validParticipantRequest, TEST_IP);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(PARTICIPANT_TOKEN);
            assertThat(response.getParticipant().getId()).isEqualTo(PARTICIPANT_ID);
            assertThat(response.getParticipant().getEmail()).isEqualTo("john.doe@example.com");

            verify(userRepository, never()).insertParticipant(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when email format is invalid")
        void invalidEmail_throwsUnprocessableEntity() {
            validParticipantRequest.setEmail("not-an-email");

            assertThatThrownBy(() -> authService.participate(validParticipantRequest, TEST_IP))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when firstName is null")
        void nullFirstName_throwsUnprocessableEntity() {
            validParticipantRequest.setFirstName(null);

            assertThatThrownBy(() -> authService.participate(validParticipantRequest, TEST_IP))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("The field firstName is required and cannot be blank.");
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when lastName is null")
        void nullLastName_throwsUnprocessableEntity() {
            validParticipantRequest.setLastName(null);

            assertThatThrownBy(() -> authService.participate(validParticipantRequest, TEST_IP))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("The field lastName is required and cannot be blank.");
        }

        @Test
        @DisplayName("accepts hyphenated name")
        void hyphenatedName_accepts() {
            validParticipantRequest.setLastName("Jean-Pierre");

            User createdUser = User.builder()
                    .id(PARTICIPANT_ID)
                    .firstName("John")
                    .lastName("Jean-Pierre")
                    .email("john.doe@example.com")
                    .role(Role.PARTICIPANT)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmailAndNames(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(userRepository.insertParticipant(anyString(), anyString(), anyString()))
                    .thenReturn(createdUser);
            when(tokenProvider.generateAccessToken(any(User.class)))
                    .thenReturn(PARTICIPANT_TOKEN);

            AuthParticipantResponseDto response = authService.participate(validParticipantRequest, TEST_IP);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(PARTICIPANT_TOKEN);
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when name contains numbers")
        void nameWithNumbers_throwsUnprocessableEntity() {
            validParticipantRequest.setFirstName("John123");

            assertThatThrownBy(() -> authService.participate(validParticipantRequest, TEST_IP))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Invalid input for firstName");
        }

        @Test
        @DisplayName("throws UnprocessableEntityException when name length exceeds 50")
        void nameTooLong_throwsUnprocessableEntity() {
            validParticipantRequest.setLastName("A" + "a".repeat(50));

            assertThatThrownBy(() -> authService.participate(validParticipantRequest, TEST_IP))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("cannot exceed");
        }
    }
}
