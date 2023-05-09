package com.atn.digital.user.config;


import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.atn.digital.user.adapters.in.handler.UserRequestHandler;
import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbClientInitializer;
import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbUserRepository;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.atn.digital.user.domain.ports.out.persistence.UserRepository;
import com.atn.digital.user.domain.services.FindUserByIdService;
import com.atn.digital.user.domain.services.RegisterNewUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Function;

public class UserDomainConfig {

    @Bean
    public UserRepository userRepositoryAdapter(DynamoDbClientInitializer initializer) {
        return new DynamoDbUserRepository(initializer.client());
    }

    @Bean
    public RegisterNewUserUseCase registerNewUserUseCase(UserRepository userRepository) {
        return new RegisterNewUserService(userRepository);
    }

    @Bean
    public FindUserByIdQuery findUserByIdQuery(UserRepository userRepository) {
        return new FindUserByIdService(userRepository);
    }

    @Bean
    public Function<Message<APIGatewayProxyRequestEvent>, APIGatewayProxyResponseEvent> userRequestHandler(
            RegisterNewUserUseCase registerNewUserUseCase, FindUserByIdQuery findUserByIdQuery) {
        return new UserRequestHandler(registerNewUserUseCase, findUserByIdQuery);
    }
}
