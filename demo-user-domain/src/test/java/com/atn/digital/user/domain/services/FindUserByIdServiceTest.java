package com.atn.digital.user.domain.services;

import com.atn.digital.user.domain.exceptions.UserNotFoundException;
import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FindUserByIdServiceTest {

    @Test
    void shouldReturnUserWhenUserIdExists() {
        FindUserByIdQuery query = new FindUserByIdService(userId ->
                User.withId(userId, "firstName", "lastName", "email@unit.test"));
        Assertions.assertNotNull(query.findByUserId(new UserId("id")));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenReturnedUserIsNull() {
        FindUserByIdQuery query = new FindUserByIdService(userId -> null);
        Assertions.assertThrows(UserNotFoundException.class, () -> query.findByUserId(new UserId("id")));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenReturnedUserHasNoId() {
        FindUserByIdQuery query = new FindUserByIdService(userId ->
                User.withoutId("firstName", "lastName", "email@unit.test"));
        Assertions.assertThrows(UserNotFoundException.class, () -> query.findByUserId(new UserId("id")));
    }
}
