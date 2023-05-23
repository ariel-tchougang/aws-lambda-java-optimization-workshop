package com.atn.digital.user.adapters.in.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.atn.digital.user.adapters.out.persistence.dynamodb.UserServiceWithPriming;
import com.atn.digital.user.domain.exceptions.ConstraintViolationException;
import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.google.gson.Gson;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Introspected
public class UserRequestHandlerWithPriming extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Logger logger = LoggerFactory.getLogger(UserRequestHandler.class);
    private final Gson gson = new Gson();
    private final UserServiceWithPriming userService;

    public UserRequestHandlerWithPriming() { this(BeanContext.run().getBean(UserServiceWithPriming.class)); }

    @Inject
    public UserRequestHandlerWithPriming(UserServiceWithPriming userService) {
        this.userService = userService;
    }

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent event) {
        String method = event.getHttpMethod().toUpperCase();
        logger.debug("httpMethod = " + method);

        switch (method) {
            case "GET" -> {
                return handleGet(event);
            }
            case "POST" -> {
                return handlePost(event);
            }
            default -> {
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
                response.setStatusCode(405);
                return response;
            }
        }
    }

    private APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
            String body = event.getBody();
            logger.debug("body = " + body);
            RegisterNewUserData userData = gson.fromJson(body, RegisterNewUserData.class);
            RegisterNewUserCommand newUserCommand = new RegisterNewUserCommand(
                    userData.firstName,
                    userData.lastName,
                    userData.email);
            User.UserId userId = userService.handle(newUserCommand);
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(201);
            response.setBody(gson.toJson(userId));
            return response;
        } catch (ConstraintViolationException e) {
            return errorApiGatewayProxyResponseEvent(e.getMessage(), 400);
        } catch (Exception e) {
            return errorApiGatewayProxyResponseEvent( e.getMessage(), 500);
        }
    }

    private APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {

        if (event.getPathParameters() == null) {
            return errorApiGatewayProxyResponseEvent("Missing parameter userId", 400);
        }

        try {
            String userId = event.getPathParameters().get("userId");
            User user = userService.findByUserId(new User.UserId(userId));
            UserDto userDto = new UserDto(
                    userId,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            );

            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(200);
            response.setBody(gson.toJson(userDto));
            return response;
        } catch (ConstraintViolationException e) {
            return errorApiGatewayProxyResponseEvent(e.getMessage(), 400);
        } catch (UserNotFoundException e) {
            return errorApiGatewayProxyResponseEvent(e.getMessage(), 404);
        } catch (Exception e) {
            return errorApiGatewayProxyResponseEvent(e.getMessage(), 500);
        }
    }

    private APIGatewayProxyResponseEvent errorApiGatewayProxyResponseEvent(String errorMessage, int statusCode) {
        logger.error("error message = " + errorMessage);
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(errorMessage);
        return response;
    }

    private record RegisterNewUserData (String firstName, String lastName, String email) { }

    public record UserDto(String id, String firstName, String lastName, String email) { }
}
