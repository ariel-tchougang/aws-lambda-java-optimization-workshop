package com.atn.digital.user.adapters.in.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.atn.digital.user.domain.exceptions.ConstraintViolationException;
import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRequestHandlerTest {

    private final Gson gson = new Gson();

    @Mock
    private RegisterNewUserUseCase useCase;

    @Mock
    private FindUserByIdQuery query;

    private UserRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserRequestHandler(useCase, query);
    }

    @Nested
    class GetWithId {
        @Test
        void shouldReturnHttpStatus200WhenUserIdExists() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            Map<String, String> pathParameters = new HashMap<>();
            String id = UUID.randomUUID().toString();
            pathParameters.put("userId", id);
            requestEvent.setPathParameters(pathParameters);

            User user = User.withId(
                    new UserId(id), "firstName", "lastName", "email"
            );
            when(query.findByUserId(any(UserId.class))).thenReturn(user);
            APIGatewayProxyResponseEvent response = handler.apply(requestEvent);

            assertEquals(200, response.getStatusCode());
            verify(query, times(1)).findByUserId(any(UserId.class));
        }

        @Test
        void shouldReturnHttpStatus404WhenUserIdDoesNotExist() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            Map<String, String> pathParameters = new HashMap<>();
            String id = UUID.randomUUID().toString();
            pathParameters.put("userId", id);
            requestEvent.setPathParameters(pathParameters);
            assertErrorResult(requestEvent, UserNotFoundException.class, 404, 1);
        }

        @Test
        void shouldReturnHttpStatus400WhenUserIdIsInvalid() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            Map<String, String> pathParameters = new HashMap<>();
            String id = "   ";
            pathParameters.put("userId", id);
            requestEvent.setPathParameters(pathParameters);
            assertErrorResult(requestEvent, ConstraintViolationException.class, 400, 0);
        }

        @Test
        void shouldReturnHttpStatus400WhenUserIdIsMissing() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("GET");
            Map<String, String> pathParameters = new HashMap<>();
            String id = UUID.randomUUID().toString();
            pathParameters.put("id", id);
            requestEvent.setPathParameters(pathParameters);
            assertErrorResult(requestEvent, ConstraintViolationException.class, 400, 0);
        }

        <T extends RuntimeException> void assertErrorResult(APIGatewayProxyRequestEvent requestEvent,
                                   Class<T> clazz, int statusCode, int expectedCallNumber) {

            if (expectedCallNumber > 0) {
                when(query.findByUserId(any(UserId.class))).thenThrow(clazz);
            }

            APIGatewayProxyResponseEvent response = handler.apply(requestEvent);
            assertEquals(statusCode, response.getStatusCode());
            verify(query, times(expectedCallNumber)).findByUserId(any(UserId.class));
        }
    }

    @Nested
    class Post {
        @Test
        void shouldReturnHttpStatus201WhenAllIsOk() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("POST");
            Map<String, String> body = new HashMap<>();
            body.put("firstName", "firstName");
            body.put("lastName", "firstName");
            body.put("email", "email@unit.test");
            requestEvent.setBody(gson.toJson(body));

            when(useCase.handle(any(RegisterNewUserCommand.class))).thenReturn(new UserId("id"));
            APIGatewayProxyResponseEvent response = handler.apply(requestEvent);

            assertEquals(201, response.getStatusCode());
            verify(useCase, times(1)).handle(any(RegisterNewUserCommand.class));
        }

        @Test
        void shouldReturnHttpStatus400WhenBodyIsInvalid() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("POST");
            Map<String, String> body = new HashMap<>();
            body.put("firstName", "firstName");
            requestEvent.setBody(gson.toJson(body));
            APIGatewayProxyResponseEvent response = handler.apply(requestEvent);

            assertEquals(400, response.getStatusCode());
            verify(useCase, times(0)).handle(any(RegisterNewUserCommand.class));
        }

        @Test
        void shouldReturnHttpStatus500WhenUnexpectedError() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("POST");
            Map<String, String> body = new HashMap<>();
            body.put("firstName", "firstName");
            body.put("lastName", "firstName");
            body.put("email", "email@unit.test");
            requestEvent.setBody(gson.toJson(body));

            when(useCase.handle(any(RegisterNewUserCommand.class))).thenThrow(new RuntimeException());
            APIGatewayProxyResponseEvent response = handler.apply(requestEvent);

            assertEquals(500, response.getStatusCode());
        }
    }

    @Nested
    class OtherMethods {
        @Test
        void shouldReturnHttpStatus405() {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod("DELETE");
            APIGatewayProxyResponseEvent response = handler.apply(requestEvent);
            assertEquals(405, response.getStatusCode());
        }
    }
}
