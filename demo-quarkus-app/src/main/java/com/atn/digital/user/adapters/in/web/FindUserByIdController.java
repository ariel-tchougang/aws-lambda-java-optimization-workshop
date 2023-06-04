package com.atn.digital.user.adapters.in.web;

import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/quarkus/users")
public class FindUserByIdController {

    @Inject
    FindUserByIdQuery query;

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByUserId(@PathParam("userId") String userId) {
        User user = query.findByUserId(new UserId(userId));
        UserDto userDto = new UserDto(
                user.getId().get().getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
        return Response.ok(userDto).build();
    }
}

