package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.auth.AuthLoginRequestDto;
import com.techindna.eventsyncapi.dto.UserResponseDto;
import com.techindna.eventsyncapi.dto.auth.AuthLoginResponseDto;
import com.techindna.eventsyncapi.entity.BlacklistedIp;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.TooManyRequestException;
import com.techindna.eventsyncapi.exception.UnauthorizedException;
import com.techindna.eventsyncapi.mapper.UserMapper;
import com.techindna.eventsyncapi.repository.BlacklistedIpRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.config.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_ATTEMPT_LIMIT = 5;

    private final DataValidator dataValidator;

    private final UserRepository userRepository;
    private final BlacklistedIpRepository blacklistedIpRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(noRollbackFor = {UnauthorizedException.class, TooManyRequestException.class})
    public AuthLoginResponseDto login(AuthLoginRequestDto request, String ipAddress, String userAgent) {
        checkBlacklist(ipAddress);

        dataValidator.validateEmail(request.getEmail());

        User admin = userRepository.findByEmail(request.getEmail())
                .filter(u -> u.getRole() == Role.ADMIN)
                .orElse(null);

        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            int attempts = incrementFailedAttempt(ipAddress, userAgent);
            if (attempts >= MAX_ATTEMPT_LIMIT) {
                throw new TooManyRequestException(
                        "You are blocked due to too many failed login attempts."
                );
            }
            throw new UnauthorizedException(
                    String.format("Invalid credentials, %d attempt(s) left.", MAX_ATTEMPT_LIMIT - attempts)
            );
        }

        blacklistedIpRepository.deleteByIpAddress(ipAddress);

        String token = tokenProvider.generateAccessToken(admin);
        UserResponseDto userDto = userMapper.toResponseDto(admin);

        return AuthLoginResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    private void checkBlacklist(String ipAddress) {
        Optional<BlacklistedIp> blacklisted = blacklistedIpRepository.findByIpAddress(ipAddress);
        if (blacklisted.isPresent() && blacklisted.get().getFailedAttempts() >= MAX_ATTEMPT_LIMIT) {
            throw new UnauthorizedException(
                    "You are not authorized to access this resource due to malicious behavior."
            );
        }
    }

    private int incrementFailedAttempt(String ipAddress, String userAgent) {
        Optional<BlacklistedIp> existing = blacklistedIpRepository.findByIpAddress(ipAddress);
        if (existing.isPresent()) {
            BlacklistedIp record = existing.get();
            if (record.getFailedAttempts() < MAX_ATTEMPT_LIMIT) {
                record.setFailedAttempts(record.getFailedAttempts() + 1);
                record.setUserAgent(userAgent);
                blacklistedIpRepository.save(record);
            }
            return record.getFailedAttempts();
        } else {
            BlacklistedIp newRecord = BlacklistedIp.builder()
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .failedAttempts(1)
                    .build();
            blacklistedIpRepository.save(newRecord);
            return 1;
        }
    }
}
