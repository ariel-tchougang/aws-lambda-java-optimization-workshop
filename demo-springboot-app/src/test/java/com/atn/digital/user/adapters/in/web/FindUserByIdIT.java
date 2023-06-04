package com.atn.digital.user.adapters.in.web;

import com.atn.digital.user.OutboundAdaptersExtension;
import com.atn.digital.user.config.UserDomainConfig;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Import({ UserDomainConfig.class })
@ExtendWith(OutboundAdaptersExtension.class)
class FindUserByIdIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RegisterNewUserUseCase newUserUseCase;

    private final String baseUrl = "/api/v1/users/{userId}";

    @Test
    void shouldFindUserByIdWhenUserIdExists() {
        RegisterNewUserCommand command = new RegisterNewUserCommand(
                "Homer",
                "Simpson",
                "homer.simpson@unit.test"
        );

        UserId userId = newUserUseCase.handle(command);
        Assertions.assertNotNull(userId);

        HttpEntity<Void> request = getVoidHttpEntity();
        ResponseEntity<UserDto> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                UserDto.class,
                userId.getId()
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDto user = response.getBody();
        Assertions.assertNotNull(user);
        Assertions.assertEquals(userId.getId(), user.id());
        Assertions.assertEquals(command.getFirstName(), user.firstName());
        Assertions.assertEquals(command.getLastName(), user.lastName());
        Assertions.assertEquals(command.getEmail(), user.email());
    }

    @Test
    void shouldReturnNotFoundWhenUserIdDoesntExist() {
        HttpEntity<Void> request = getVoidHttpEntity();
        ResponseEntity<String> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                String.class,
                "userId"
        );

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnMethodNotAllowedWhenPathParamIsNull() {
        HttpEntity<Void> request = getVoidHttpEntity();
        ResponseEntity<String> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                String.class,
                (String)null
        );

        Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnMethodNotAllowedWhenPathParamIsEmpty() {
        HttpEntity<Void> request = getVoidHttpEntity();
        ResponseEntity<String> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                String.class,
                ""
        );

        Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnBadRequestWhenPathParamIsBlank() {
        HttpEntity<Void> request = getVoidHttpEntity();
        ResponseEntity<String> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                request,
                String.class,
                "   "
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    private static HttpEntity<Void> getVoidHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(null, headers);
    }
}
