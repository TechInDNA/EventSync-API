package com.techindna.eventsyncapi.dto.speaker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakerUpdateInputDto {
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private String bio;
}
