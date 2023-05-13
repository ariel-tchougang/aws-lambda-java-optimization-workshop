## Migrate spring boot app to AWS Lambda using Spring Cloud Function

The work has already been done, but in a nutshell here are the steps I took to achieve it.

### Converting the initial Spring boot app to AWS Lambda with Spring Cloud Function

#### Removing spring-boot-starter-web and adding spring-cloud-function-adapter-aws & aws-lambda-java-events

```xml
<!-- Remove 
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
 -->

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-function-adapter-aws</artifactId>
    <version>3.2.10</version>
</dependency>

<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-events</artifactId>
    <version>3.11.1</version>
</dependency>

```

#### Create a UserRequestHandler to manage incoming request from Amazon API Gateway

```java
public class UserRequestHandler implements Function<Message<APIGatewayProxyRequestEvent>, APIGatewayProxyResponseEvent> {
    private final Gson gson = new Gson();

    private final RegisterNewUserUseCase registerNewUserUseCase;

    private final FindUserByIdQuery findUserByIdQuery;

    public UserRequestHandler(RegisterNewUserUseCase registerNewUserUseCase, FindUserByIdQuery findUserByIdQuery) {
        this.registerNewUserUseCase = registerNewUserUseCase;
        this.findUserByIdQuery = findUserByIdQuery;
    }

    @Override
    public APIGatewayProxyResponseEvent apply(Message<APIGatewayProxyRequestEvent> message) {
        APIGatewayProxyRequestEvent event = message.getPayload();
        String method = event.getHttpMethod().toUpperCase();
        Context context = message.getHeaders().get("aws-context", Context.class);
        context.getLogger().log("httpMethod = " + method);

        switch (method) {
            case "GET" -> {
                return handleGet(event, context);
            }
            case "POST" -> {
                return handlePost(event, context);
            }
            default -> {
                APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
                response.setStatusCode(405);
                return response;
            }
        }
    }
    
    ....
    ....
}
```
