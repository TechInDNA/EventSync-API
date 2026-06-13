package com.techindna.eventsyncapi.dto.speaker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakerResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private String bio;
    private List<ExternalLinkDto> externalLinks;
}
