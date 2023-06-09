package com.atn.digital.user.adapters.out.persistence.dynamodb;

import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.atn.digital.user.domain.ports.out.persistence.UserRepository;
import com.atn.digital.user.domain.services.FindUserByIdService;
import com.atn.digital.user.domain.services.RegisterNewUserService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserService {
    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final FindUserByIdQuery findUserByIdQuery;

    @Inject
    public UserService(DynamoDbClientInitializer initializer) {
        UserRepository adapter = new DynamoDbUserRepository(initializer.client());
        registerNewUserUseCase = new RegisterNewUserService(adapter);
        findUserByIdQuery = new FindUserByIdService(adapter);
    }

    public UserId handle(RegisterNewUserCommand registerNewUserCommand) {
        return registerNewUserUseCase.handle(registerNewUserCommand);
    }

    public User findByUserId(UserId userId) throws UserNotFoundException {
        return findUserByIdQuery.findByUserId(userId);
    }
}
