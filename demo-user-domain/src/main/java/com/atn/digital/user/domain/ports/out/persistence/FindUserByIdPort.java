package com.atn.digital.user.domain.ports.out.persistence;

import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;

public interface FindUserByIdPort {
    User findByUserId(UserId userId);
}
