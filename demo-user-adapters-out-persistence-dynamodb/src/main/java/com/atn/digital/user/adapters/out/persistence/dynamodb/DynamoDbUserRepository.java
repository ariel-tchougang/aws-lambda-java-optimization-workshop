package com.atn.digital.user.adapters.out.persistence.dynamodb;

import com.atn.digital.user.domain.models.User;
import com.atn.digital.user.domain.models.User.UserId;
import com.atn.digital.user.domain.ports.out.persistence.UserRepository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.UUID;

public class DynamoDbUserRepository extends UserRepository {

    private final DynamoDbTable<UserEntity> userTable;
    private final UserEntityMapper mapper = new UserEntityMapper();

    public DynamoDbUserRepository(DynamoDbClient client) {
        userTable = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build()
                .table(System.getProperty("USER_TABLE"), TableSchema.fromBean(UserEntity.class));
    }

    public UserId registerNewUser(User user) {
        var userId = new UserId(UUID.randomUUID().toString());
        User dbUser = User.withId(
                userId,
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
        userTable.putItem(mapper.toUserEntity(dbUser));
        return userId;
    }

    UserEntity findByUserId(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return userTable.getItem(key);
    }

    public User findByUserId(UserId userId) {
        UserEntity userEntity = findByUserId(userId.getId());
        return mapper.toUser(userEntity);
    }
}
