package com.atn.digital.user.config;


import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbClientInitializer;
import com.atn.digital.user.adapters.out.persistence.dynamodb.DynamoDbUserRepository;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.atn.digital.user.domain.ports.out.persistence.UserRepository;
import com.atn.digital.user.domain.services.FindUserByIdService;
import com.atn.digital.user.domain.services.RegisterNewUserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;


@ApplicationScoped
public class UserDomainConfig {

    @Inject
    DynamoDbClientInitializer initializer;

    @Produces
    UserRepository userRepositoryAdapter() {
        return new DynamoDbUserRepository(initializer.client());
    }

    @Produces
    public RegisterNewUserUseCase registerNewUserUseCase(UserRepository userRepository) {
        return new RegisterNewUserService(userRepository);
    }

    @Produces
    public FindUserByIdQuery findUserByIdQuery(UserRepository userRepository) {
        return new FindUserByIdService(userRepository);
    }
}
