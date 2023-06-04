package com.atn.digital.user.adapters.in.handler;

import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User.UserId;
import jakarta.inject.Named;
import org.crac.Core;
import org.crac.Resource;

import java.util.UUID;

@Named("userRequestHandlerWithPriming")
public class UserRequestHandlerWithPriming extends UserRequestHandler implements Resource {

    public UserRequestHandlerWithPriming() {
        Core.getGlobalContext().register(this);
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
}

