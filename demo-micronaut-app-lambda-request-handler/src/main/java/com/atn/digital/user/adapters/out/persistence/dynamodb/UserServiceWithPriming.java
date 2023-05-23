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

import io.micronaut.crac.OrderedResource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.crac.Context;
// import org.crac.Core;
import org.crac.Resource;

import java.util.UUID;

@Singleton
public class UserServiceWithPriming implements OrderedResource {
    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final FindUserByIdQuery findUserByIdQuery;

    @Inject
    public UserServiceWithPriming(DynamoDbClientInitializer initializer) {
        UserRepository adapter = new DynamoDbUserRepository(initializer.client());
        registerNewUserUseCase = new RegisterNewUserService(adapter);
        findUserByIdQuery = new FindUserByIdService(adapter);
        // Core.getGlobalContext().register(this);
    }

    public UserId handle(RegisterNewUserCommand registerNewUserCommand) {
        return registerNewUserUseCase.handle(registerNewUserCommand);
    }

    public User findByUserId(UserId userId) throws UserNotFoundException {
        return findUserByIdQuery.findByUserId(userId);
    }
    
    @Override
	public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
		System.out.println("Before Checkpoint");
		try {
		    findByUserId(new UserId(UUID.randomUUID().toString()));
		} catch (UserNotFoundException unfe) {
		    // Expected
		}
	    System.out.println("After Checkpoint");
	}

	@Override
	public void afterRestore(Context<? extends Resource> context) throws Exception {
		System.out.println("After Restore");	
	}
}
