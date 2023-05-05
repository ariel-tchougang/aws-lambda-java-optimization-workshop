package com.atn.digital.user.adapters.out.persistence.dynamodb;

import com.atn.digital.user.domain.ports.in.queries.FindUserByIdQuery;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserCommand;
import com.atn.digital.user.domain.ports.in.usecases.RegisterNewUserUseCase;
import com.atn.digital.user.domain.services.FindUserByIdService;
import com.atn.digital.user.domain.services.RegisterNewUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Testcontainers
class PersistenceDynamoDbIT {

    private String dynamoDbLocalUri;

    @Container
    GenericContainer dynamoDBLocal =
            new GenericContainer("amazon/dynamodb-local:latest")
                    .withExposedPorts(8000);

    @BeforeEach
    public void setUp() {
        System.setProperty("USER_TABLE", "Users");
        dynamoDbLocalUri = "http://localhost:" + dynamoDBLocal.getFirstMappedPort();
    }

    @Test
    void shouldAddNewUserInTable() {
        DynamoDbClient client = DynamoDbTableCreator.initialize(dynamoDbLocalUri);
        DynamoDbUserRepository userRepository = new DynamoDbUserRepository(client);
        RegisterNewUserUseCase newUserService = new RegisterNewUserService(userRepository);
        RegisterNewUserCommand newUserCmd = new RegisterNewUserCommand(
                "Homer",
                "Simpson",
                "homer.simpson@unit.test"
        );

        var userId = newUserService.handle(newUserCmd);
        Assertions.assertNotNull(userId);
        Assertions.assertNotNull(userId.getId());

        FindUserByIdQuery query = new FindUserByIdService(userRepository);
        var user = query.findByUserId(userId);
        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertTrue(user.getId().isPresent());
        Assertions.assertEquals(userId.getId(), user.getId().get().getId());
    }
}
