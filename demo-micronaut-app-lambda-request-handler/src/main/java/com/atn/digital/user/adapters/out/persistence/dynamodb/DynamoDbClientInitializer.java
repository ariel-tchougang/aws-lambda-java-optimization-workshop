package com.atn.digital.user.adapters.out.persistence.dynamodb;

import jakarta.inject.Singleton;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DynamoDbClientInitializer {
    private DynamoDbClient client;

    public DynamoDbClient client() { return client; }

    @PostConstruct
    private void initialize() {

        String uri = System.getProperty("LOCAL_DYNAMODB_URI");
        String tableName = System.getProperty("USER_TABLE");
        boolean isLocalDynamoDbUri = uri != null && !uri.isBlank();
        
        if (isLocalDynamoDbUri) {
            client = DynamoDbClient.builder().endpointOverride(URI.create(uri)).build();
            createTable(client, tableName);
            return;
        }
        
        client = DynamoDbClient.builder().build();
        tableName = System.getenv("TABLE_NAME");
        
        if (tableName == null) {
            tableName = "WorkshopUsers";
        }
        
        System.setProperty("USER_TABLE", tableName);
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }

    private boolean tableExists(DynamoDbClient client, String tableName) {
        return client.listTables().tableNames().contains(tableName);
    }

    private void createTable(DynamoDbClient client, String tableName) {
        
        if (tableExists(client, tableName)) {
            return;    
        }

        List<KeySchemaElement> keySchema = new ArrayList<>();
        keySchema.add(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH.name()).build());

        List<AttributeDefinition> attributes = new ArrayList<>();
        attributes.add(AttributeDefinition.builder().attributeName("id")
                .attributeType(ScalarAttributeType.S.name()).build());

        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(keySchema)
                .attributeDefinitions(attributes)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        try {
            client.createTable(request);
            System.out.printf("Table %s created successfully.%n", tableName);

            DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();

            DynamoDbWaiter dbWaiter = client.waiter();
            WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);

        } catch (DynamoDbException e) {
            System.err.printf("Failed to create table %s%n", tableName);
            throw e;
        }
    }
}
