## Migrate spring boot app to AWS Lambda using Plain Java and AWS Lambda Request Handler

The work has already been done, but in a nutshell here are the steps I took to achieve it.

### AWS Lambda using AWS Lambda Request Handler

#### Add appropriate AWS Lambda dependencies

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-core</artifactId>
    <version>1.2.2</version>
</dependency>
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-lambda-java-events</artifactId>
    <version>3.11.1</version>
</dependency>
```

#### Create a UserRequestHandler to manage incoming request from Amazon API Gateway

```java
 public class UserRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final UserRepositoryAdapter repository;
    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final FindUserByIdQuery findUserByIdQuery;

    private final Gson gson = new Gson();

    public UserRequestHandler() {
        this(DynamoDbClient.builder().build());
    }

    public UserRequestHandler(DynamoDbClient client) {
        repository = new DynamoDbUserRepository(client);
        registerNewUserUseCase = new RegisterNewUserService(repository);
        findUserByIdQuery = new FindUserByIdService(repository);
        System.setProperty("USER_TABLE", TABLE_NAME);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String method = event.getHttpMethod().toUpperCase();
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

