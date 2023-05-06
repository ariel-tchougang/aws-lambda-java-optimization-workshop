package com.atn.digital.user.adapters.in.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.atn.digital.user.domain.exceptions.ConstraintViolationException;
import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.Message;

import java.util.function.Function;

public class UserRequestHandler implements Function<Message<APIGatewayProxyRequestEvent>, APIGatewayProxyResponseEvent> {
    private final Gson gson = new Gson();

    private final RegisterNewUserUseCase registerNewUserUseCase;

    private final FindUserByIdQuery findUserByIdQuery;

    public UserRequestHandler(RegisterNewUserUseCase registerNewUserUseCase, FindUserByIdQuery findUserByIdQuery) {
        this.registerNewUserUseCase = registerNewUserUseCase;
        this.findUserByIdQuery = findUserByIdQuery;
    }

    @Override
    public APIGatewayProxyResponseEvent apply(Message<APIGatewayProxyRequestEvent> message) {
        APIGatewayProxyRequestEvent event = message.getPayload();
        String method = event.getHttpMethod().toUpperCase();
        Context context = message.getHeaders().get("aws-context", Context.class);
        context.getLogger().log("httpMethod = " + method);

        switch (method) {
            case "GET" -> {
                return handleGet(event, context);
            }
            case "POST" -> {
                return handlePost(event, context);
            }
            default -> {
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
                response.setStatusCode(405);
                return response;
            }
        }
    }

    private APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event, Context context) {
        try {
            String body = event.getBody();
            context.getLogger().log("body = " + body);
            RegisterNewUserData userData = gson.fromJson(body, RegisterNewUserData.class);
            RegisterNewUserCommand newUserCommand = new RegisterNewUserCommand(
                    userData.firstName,
                    userData.lastName,
                    userData.email);
            User.UserId userId = registerNewUserUseCase.handle(newUserCommand);
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(201);
            response.setBody(gson.toJson(userId));
            return response;
        } catch (ConstraintViolationException e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 400);
        } catch (Exception e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 500);
        }
    }

    private APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event, Context context) {

        if (event.getPathParameters() == null) {
            return errorApiGatewayProxyResponseEvent(context, "Missing parameter userId", 400);
        }

        try {
            String userId = event.getPathParameters().get("userId");
            User user = findUserByIdQuery.findByUserId(new User.UserId(userId));
            UserDto userDto = new UserDto(
                    user.getId().get().getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            );

            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setBody(gson.toJson(userDto));
            return response;
        } catch (ConstraintViolationException e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 400);
        } catch (UserNotFoundException e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 404);
        } catch (Exception e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 500);
        }
    }

    private APIGatewayProxyResponseEvent errorApiGatewayProxyResponseEvent(Context context, String errorMessage, int statusCode) {
        context.getLogger().log("error message = " + errorMessage);
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(errorMessage);
        return response;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private class RegisterNewUserData {
        private String firstName;
        private String lastName;
        private String email;
    }

    public record UserDto(String id, String firstName, String lastName, String email) { }
}
