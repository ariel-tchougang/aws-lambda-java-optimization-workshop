package com.atn.digital.user.adapters.in.web;

import com.atn.digital.user.OutboundAdaptersExtension;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.out.persistence.FindUserByIdPort;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;

@ExtendWith(OutboundAdaptersExtension.class)
@QuarkusTest
class RegisterNewUserIT {

    @Inject
    FindUserByIdPort findUserByIdPort;

    final String baseUrl = "/quarkus/users";

    @Test
    void shouldRegisterNewUserWhenWebInputIsValid() {
        RegisterNewUserWeb webInput = new RegisterNewUserWeb(
                "Homer",
                "Simpson",
                "homer.simpson@unit.test"
        );

        UserIdDto response = given()
                .when()
                .contentType(ContentType.JSON)
                .body(webInput)
                .post(baseUrl)
                .then()
                .statusCode(Status.CREATED.getStatusCode())
                .extract().as(UserIdDto.class);

        User user = findUserByIdPort.findByUserId(new UserId(response.id()));
        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.getFirstName(), webInput.getFirstName());
        Assertions.assertEquals(user.getLastName(), webInput.getLastName());
        Assertions.assertEquals(user.getEmail(), webInput.getEmail());
    }

    @Test
    void shouldReturnBadRequestWhenWebInputIsNull() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .post(baseUrl)
                .then()
                .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenWebInputIsNotValid() {
        RegisterNewUserWeb webInput = new RegisterNewUserWeb("", "", "");

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(webInput)
                .post(baseUrl)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }
}
