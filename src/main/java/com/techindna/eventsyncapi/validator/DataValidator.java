package com.techindna.eventsyncapi.validator;


import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DataValidator {
    private static final int TEXT_MAX_LENGTH = 1000;
    private static final int URL_MAX_LENGTH = 255;
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z]+){1,2}$");
    private static final Pattern ALLOWED_EMAIL_CHAR = Pattern.compile("^[a-zA-Z0-9.@_-]+$");
    private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z][a-zA-Z' -]+[a-zA-Z]+$");
    private static final Pattern ALLOWED_NAME_CHAR = Pattern.compile("^[a-zA-Z-' ]+$");
    private static final Pattern ALLOWED_SEARCH_STRING = Pattern.compile("^[A-Za-z0-9' -]*$");
    private static final Pattern ALLOWED_TEXT_CHARS = Pattern.compile("^[A-Za-z0-9.,;\"!'? -]*$");
    private final Pattern VALID_URL = Pattern.compile("^https?://[a-zA-Z0-9\\-._%&?#/]+$");
    private final Pattern ALLOWED_STRING_CHAR = Pattern.compile("^[0-9a-zA-Z-'. ]+$");

    public void checkNullData(String fieldName, Object data){
        if (data == null){
            throw new UnprocessableEntityException(String.format("The field %s is required.", fieldName));
        }
    }

    protected void lengthValidation(String fieldName, int limit, String data){
        if (data != null && data.length() >= limit){
            throw new UnprocessableEntityException(
                    String.format("The length of %s field cannot exceed %d.", fieldName, limit)
            );
        }
    }

    public void validateSearchString(String data){
        if (data != null && !ALLOWED_SEARCH_STRING.matcher(data).matches()){
            throw new UnprocessableEntityException(
                    "Invalid input for Search field: only a-zA-Z0-9-' characters are allowed."
            );
        }
    }

    public void validateName(String fieldName, String data){
        checkNullData(fieldName, data);

        lengthValidation(fieldName, 50, data);

        if (!ALLOWED_NAME_CHAR.matcher(data).matches()){
            throw new UnprocessableEntityException(
                    String.format("Invalid input for %s: only a-zA-Z-' characters are allowed.", fieldName)
            );
        }

        if (!VALID_NAME.matcher(data).matches()){
            throw new UnprocessableEntityException(
                    String.format("Invalid name format: '%s'", data)
            );
        }
    }
    public void validateEmail(String email){
        checkNullData("email", email);
        lengthValidation("email", 50, email);

        if (!ALLOWED_EMAIL_CHAR.matcher(email).matches()){
            throw new UnprocessableEntityException(String.format("Invalid input for email: '%s' only a-zA-Z0-9@_.- characters are allowed.", email));
        }

        if (!VALID_EMAIL_PATTERN.matcher(email).matches()){
            throw new UnprocessableEntityException(String.format("Invalid email format: '%s'", email));
        }
    }

    public void validateText(String fieldName, String text){
        checkNullData(fieldName, text);

        if (text.isBlank()){
            throw new UnprocessableEntityException(
                    String.format("The field %s cannot be blank.", fieldName)
            );
        }

        lengthValidation(fieldName, TEXT_MAX_LENGTH, text);

        if (!ALLOWED_TEXT_CHARS.matcher(text).matches()){
            throw new UnprocessableEntityException("Invalid input for bio field: only A-Za-z0-9.,;\"!'- characters are allowed.");
        }
    }

    public void validateString(String fieldName, String data){
        checkNullData(fieldName, data);

        if (data.isBlank()){
            throw new UnprocessableEntityException(
                    String.format("The field %s is required and cannot be blank.", fieldName)
            );
        }

        lengthValidation(fieldName, 50, data);
        if (!ALLOWED_STRING_CHAR.matcher(data).matches()){
            throw new UnprocessableEntityException(
                    String.format("Invalid input for %s field: only a-zA-Z0-9-' characters are allowed.",  fieldName)
            );
        }
    }

    public void validateUrl(String fieldName, String url){
        checkNullData(fieldName, url);

        lengthValidation(fieldName, URL_MAX_LENGTH, url);

        if (!VALID_URL.matcher(url).matches()){
            throw new UnprocessableEntityException(String.format("Invalid URL format or '%s' contain forbidden characters, only a-zA-Z0-9-?._%%&# characters are allowed", url));
        }
    }

}
