package com.atn.digital.user.adapters.in.web;

import com.atn.digital.user.OutboundAdaptersExtension;
import com.atn.digital.user.config.UserDomainConfig;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.out.persistence.FindUserByIdPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Import({ UserDomainConfig.class })
@ExtendWith(OutboundAdaptersExtension.class)
class RegisterNewUserIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FindUserByIdPort findUserByIdPort;

    private final String baseUrl = "/serverless-java-container/users";

    @Test
    void shouldRegisterNewUserWhenWebInputIsValid() {
        RegisterNewUserWeb webInput = new RegisterNewUserWeb(
                "Homer",
                "Simpson",
                "homer.simpson@unit.test"
        );
        HttpEntity<RegisterNewUserWeb> request = getHttpEntity(webInput);
        ResponseEntity<UserIdDto> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                UserIdDto.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        UserIdDto userId = response.getBody();
        Assertions.assertNotNull(userId);

        User user = findUserByIdPort.findByUserId(new UserId(userId.id()));
        Assertions.assertNotNull(user);
        Assertions.assertTrue(user.getId().isPresent());
        Assertions.assertEquals(user.getId().get().getId(), userId.id());
        Assertions.assertEquals(webInput.getFirstName(), user.getFirstName());
        Assertions.assertEquals(webInput.getLastName(), user.getLastName());
        Assertions.assertEquals(webInput.getEmail(), user.getEmail());
    }

    @Test
    void shouldReturnBadRequestWhenWebInputIsNull() {
        HttpEntity<RegisterNewUserWeb> request = getHttpEntity(null);
        ResponseEntity<UserIdDto> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                UserIdDto.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertNull(response.getBody().id());
    }

    @Test
    void shouldReturnBadRequestWhenWebInputIsNotValid() {
        RegisterNewUserWeb webInput = new RegisterNewUserWeb("","", "");
        HttpEntity<RegisterNewUserWeb> request = getHttpEntity(webInput);
        ResponseEntity<String> response =  restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    private static HttpEntity<RegisterNewUserWeb> getHttpEntity(RegisterNewUserWeb webInput) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(webInput, headers);
    }
}
