package com.techindna.eventsyncapi.validator;


import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DataValidator {
    private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z]+){1,2}$");
    private static final Pattern ALLOWED_EMAIL_CHAR = Pattern.compile("^[a-zA-Z0-9.@_-]+$");
    private static final Pattern VALID_NAME = Pattern.compile("^[A-Z][a-zA-Z' -]+[a-zA-Z]+$");
    private static final Pattern ALLOWED_NAME_CHAR = Pattern.compile("^[a-zA-Z-' ]+$");

    public void checkNullData(String fieldName, String data){
        if (data == null || data.isEmpty() || data.isBlank()){
            throw new UnprocessableEntityException(String.format("The field %s is required and cannot be blank.", fieldName));
        }
    }

    protected void lengthValidation(String fieldName, int limit, String data){
        if (data != null && data.length() > limit){
            throw new UnprocessableEntityException(
                    String.format("The length of %s field cannot exceed %d.", fieldName, limit)
            );
        }
    }

    public void validateName(String fieldName, String data, boolean isRequired){
        if (isRequired){
            checkNullData(fieldName, data);
        }

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

}
