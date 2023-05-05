package com.atn.digital.user.domain.ports.in.usecases;

import com.atn.digital.user.domain.validation.ValidationUtils;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
public class RegisterNewUserCommand {

    private final String firstName;

    private final String lastName;

    private final String email;

    public RegisterNewUserCommand(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        ValidationUtils.validateNotNullNotBlank("firstName", firstName);
        ValidationUtils.validateNotNullNotBlank("lastName", lastName);
        ValidationUtils.validateNotNullNotBlank("email", email);
        ValidationUtils.validateEmail("email", email);
    }
}
