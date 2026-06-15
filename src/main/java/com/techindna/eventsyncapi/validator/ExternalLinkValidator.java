package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExternalLinkValidator {

    private final DataValidator dataValidator;

    public void externalLinkValidator(List<ExternalLinkDto> externalLinks) {
        if (externalLinks != null && !externalLinks.isEmpty()) {
            for (ExternalLinkDto linkDto : externalLinks) {
                dataValidator.validateName("name", linkDto.getName());
                dataValidator.validateUrl("url", linkDto.getUrl());
            }
        }
    }
}
