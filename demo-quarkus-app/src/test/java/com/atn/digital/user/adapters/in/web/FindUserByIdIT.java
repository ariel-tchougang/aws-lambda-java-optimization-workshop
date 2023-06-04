package com.atn.digital.user.adapters.in.web;

import com.atn.digital.user.OutboundAdaptersExtension;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;

@ExtendWith(OutboundAdaptersExtension.class)
@QuarkusTest
class FindUserByIdIT {

    @Inject
    RegisterNewUserUseCase newUserUseCase;

    final String baseUrl = "/quarkus/users/{userId}";

    @Test
    void shouldFindUserByIdWhenUserIdExists() {
        RegisterNewUserCommand command = new RegisterNewUserCommand(
                "Homer",
                "Simpson",
                "homer.simpson@unit.test"
        );

        UserId userId = newUserUseCase.handle(command);
        Assertions.assertNotNull(userId);

        UserDto user = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId.getId())
                .when().get(baseUrl)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(UserDto.class);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(userId.getId(), user.id());
        Assertions.assertEquals(command.getFirstName(), user.firstName());
        Assertions.assertEquals(command.getLastName(), user.lastName());
        Assertions.assertEquals(command.getEmail(), user.email());
    }

    @Test
    void shouldReturnNotFoundWhenUserIdDoesntExist() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", "userId")
                .when().get(baseUrl)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void shouldReturnMethodNotAllowedWhenPathParamIsEmpty() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", "")
                .when().get(baseUrl)
                .then()
                .statusCode(Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenPathParamIsBlank() {
        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", "   ")
                .when().get(baseUrl)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
