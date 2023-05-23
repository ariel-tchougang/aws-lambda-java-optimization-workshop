package com.atn.digital.user.adapters.in.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbUserRepository;
import com.atn.digital.user.domain.exceptions.ConstraintViolationException;
import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.atn.digital.user.domain.ports.out.persistence.UserRepository;
import com.atn.digital.user.domain.services.FindUserByIdService;
import com.atn.digital.user.domain.services.RegisterNewUserService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.crac.Core;
import org.crac.Resource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.UUID;

@AllArgsConstructor
public class UserRequestHandlerWithPriming implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>, Resource {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final UserRepository repository;
    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final FindUserByIdQuery findUserByIdQuery;

    private final Gson gson = new Gson();

    public UserRequestHandlerWithPriming() {
        this(DynamoDbClient.builder().build());
    }

    public UserRequestHandlerWithPriming(DynamoDbClient client) {
        repository = new DynamoDbUserRepository(client);
        registerNewUserUseCase = new RegisterNewUserService(repository);
        findUserByIdQuery = new FindUserByIdService(repository);
        Core.getGlobalContext().register(this);
        System.setProperty("USER_TABLE", TABLE_NAME);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String method = event.getHttpMethod().toUpperCase();
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
    
    @Override
	public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
		System.out.println("Before Checkpoint");
		try {
		    findUserByIdQuery.findByUserId(new UserId(UUID.randomUUID().toString()));
		} catch (UserNotFoundException unfe) {
		    // Expected
		}
	    System.out.println("After Checkpoint");
	}

	@Override
	public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {
		System.out.println("After Restore");	
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
            UserId userId = registerNewUserUseCase.handle(newUserCommand);
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
            User user = findUserByIdQuery.findByUserId(new UserId(userId));
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
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 400);
        } catch (UserNotFoundException e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 404);
        } catch (Exception e) {
            return errorApiGatewayProxyResponseEvent(context, e.getMessage(), 500);
        }
    }

    private APIGatewayProxyResponseEvent errorApiGatewayProxyResponseEvent(
            Context context, String errorMessage, int statusCode) {
        context.getLogger().log("error message = " + errorMessage);
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(errorMessage);
        return response;
    }

    private record RegisterNewUserData (String firstName, String lastName, String email) { }

    public record UserDto(String id, String firstName, String lastName, String email) { }
}

