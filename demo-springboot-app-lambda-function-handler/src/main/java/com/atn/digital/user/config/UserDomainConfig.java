package com.atn.digital.user.config;


import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.atn.digital.user.adapters.in.handler.UserRequestHandler;
import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbClientInitializer;
import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbUserRepository;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.atn.digital.user.domain.ports.out.persistence.UserRepositoryAdapter;
import com.atn.digital.user.domain.services.FindUserByIdService;
import com.atn.digital.user.domain.services.RegisterNewUserService;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

public class UserDomainConfig {

    @Bean
    public UserRepositoryAdapter userRepositoryAdapter(DynamoDbClientInitializer initializer) {
        return new DynamoDbUserRepository(initializer.client());
    }

    @Bean
    public RegisterNewUserUseCase registerNewUserUseCase(UserRepositoryAdapter userRepositoryAdapter) {
        return new RegisterNewUserService(userRepositoryAdapter);
    }

    @Bean
    public FindUserByIdQuery findUserByIdQuery(UserRepositoryAdapter userRepositoryAdapter) {
        return new FindUserByIdService(userRepositoryAdapter);
    }

    @Bean
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> userRequestHandler(
            RegisterNewUserUseCase registerNewUserUseCase, FindUserByIdQuery findUserByIdQuery) {
        return new UserRequestHandler(registerNewUserUseCase, findUserByIdQuery);
    }
}
