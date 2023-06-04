package com.atn.digital.user.adapters.in.web;

import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/quarkus/users")
public class RegisterNewUserController {

    @Inject
    RegisterNewUserUseCase useCase;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerNewUser(RegisterNewUserWeb newUserWeb) {
        RegisterNewUserCommand newUserCommand = new RegisterNewUserCommand(
                newUserWeb.getFirstName(),
                newUserWeb.getLastName(),
                newUserWeb.getEmail());
        UserId userId = useCase.handle(newUserCommand);
        return Response.status(Response.Status.CREATED).entity(new UserIdDto(userId.getId())).build();
    }
}
