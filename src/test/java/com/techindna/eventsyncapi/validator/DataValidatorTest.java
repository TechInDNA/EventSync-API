package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataValidatorTest {

    private final DataValidator validator = new DataValidator();

    @Nested
    @DisplayName("validateName")
    class ValidateName {

        @Test
        @DisplayName("accepts simple name starting with uppercase")
        void simpleName_accepts() {
            validator.validateName("firstName", "John", true);
        }

        @Test
        @DisplayName("rejects null when required")
        void nullRequired_rejects() {
            assertThatThrownBy(() -> validator.validateName("firstName", null, true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("The field firstName is required and cannot be blank.");
        }

        @Test
        @DisplayName("rejects blank when required")
        void blankRequired_rejects() {
            assertThatThrownBy(() -> validator.validateName("firstName", "", true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("The field firstName is required and cannot be blank.");
        }

        @Test
        @DisplayName("rejects name with numbers")
        void numbers_rejects() {
            assertThatThrownBy(() -> validator.validateName("firstName", "John123", true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Invalid input for firstName");
        }

        @Test
        @DisplayName("rejects name longer than 50 characters")
        void tooLong_rejects() {
            String longName = "A" + "a".repeat(50);

            assertThatThrownBy(() -> validator.validateName("firstName", longName, true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("cannot exceed");
        }

        @Test
        @DisplayName("rejects lowercase first letter")
        void lowercaseStart_rejects() {
            assertThatThrownBy(() -> validator.validateName("firstName", "john", true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("Invalid name format: 'john'");
        }

        @Test
        @DisplayName("accepts hyphenated name like Jean-Pierre")
        void hyphenatedName_accepts() {
            validator.validateName("firstName", "Jean-Pierre", true);
        }

        @Test
        @DisplayName("accepts name with apostrophe like O'Brien")
        void apostropheName_accepts() {
            validator.validateName("lastName", "O'Brien", true);
        }

        @Test
        @DisplayName("accepts multi-word name like De La Cruz")
        void multiWordName_accepts() {
            validator.validateName("lastName", "De La Cruz", true);
        }

        @Test
        @DisplayName("rejects name ending with hyphen")
        void endsWithHyphen_rejects() {
            assertThatThrownBy(() -> validator.validateName("firstName", "John-", true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("Invalid name format: 'John-'");
        }

        @Test
        @DisplayName("rejects name ending with apostrophe")
        void endsWithApostrophe_rejects() {
            assertThatThrownBy(() -> validator.validateName("firstName", "John'", true))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessage("Invalid name format: 'John''");
        }
    }

    @Nested
    @DisplayName("validateEmail")
    class ValidateEmail {

        @Test
        @DisplayName("accepts valid email")
        void validEmail_accepts() {
            validator.validateEmail("user@example.com");
        }

        @Test
        @DisplayName("rejects invalid format")
        void invalidFormat_rejects() {
            assertThatThrownBy(() -> validator.validateEmail("not-an-email"))
                    .isInstanceOf(UnprocessableEntityException.class)
                    .hasMessageContaining("Invalid email format");
        }
    }
}
