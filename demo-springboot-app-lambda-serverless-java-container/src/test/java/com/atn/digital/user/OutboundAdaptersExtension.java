package com.atn.digital.user;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

public class OutboundAdaptersExtension implements BeforeAllCallback, AfterAllCallback {
    private GenericContainer dynamoDBLocal;

    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        dynamoDBLocal = new GenericContainer("amazon/dynamodb-local:latest").withExposedPorts(8000);
        dynamoDBLocal.start();
        System.setProperty("LOCAL_DYNAMODB_URI", "http://localhost:" + dynamoDBLocal.getFirstMappedPort());
        System.setProperty("USER_TABLE", "Users");
    }

    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (dynamoDBLocal != null) {
            dynamoDBLocal.stop();
        }
    }
}
