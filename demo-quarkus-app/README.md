## Migrate spring boot app to AWS Lambda using Serverless Java Container

The work has already been done, but in a nutshell here are the steps I took to achieve it.

### Converting the initial Spring boot app to AWS Lambda with serverless java container
#### Add the AWS Serverless Java Container library

```xml
<dependency>
    <groupId>com.amazonaws.serverless</groupId>
    <artifactId>aws-serverless-java-container-core</artifactId>
    <version>1.9.3</version>
</dependency>
```

#### Create a StreamLambdaHandler to wrap around Springboot UserApplication

```java
public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = new SpringBootProxyHandlerBuilder<AwsProxyRequest>()
                    .defaultProxy()
                    .asyncInit()
                    .springBootApplication(UserApplication.class)
                    .buildAndInitialize();
        } catch (ContainerInitializationException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}
```
