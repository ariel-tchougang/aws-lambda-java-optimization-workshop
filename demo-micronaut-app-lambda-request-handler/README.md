## Migrate spring boot app to AWS Lambda using Micronaut Request Handler

The work has already been done, but in a nutshell here are the steps I took to achieve it.

### AWS Lambda with Micronaut Request Handler

#### Create a UserRequestHandler to manage incoming request from Amazon API Gateway

```java
@Introspected
public class UserRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Logger logger = LoggerFactory.getLogger(UserRequestHandler.class);
    private final Gson gson = new Gson();
    private final UserService userService;

    public UserRequestHandler() { this(BeanContext.run().getBean(UserService.class)); }

    @Inject
    public UserRequestHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent event) {
        String method = event.getHttpMethod().toUpperCase();
        logger.debug("httpMethod = " + method);

        switch (method) {
            case "GET" -> {
                return handleGet(event);
            }
            case "POST" -> {
                return handlePost(event);
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

#### Create a UserService class

```java
@Singleton
public class UserService {
    private final RegisterNewUserUseCase registerNewUserUseCase;
    private final FindUserByIdQuery findUserByIdQuery;

    @Inject
    public UserService(DynamoDbClientInitializer initializer) {
        UserRepositoryAdapter adapter = new DynamoDbUserRepository(initializer.client());
        registerNewUserUseCase = new RegisterNewUserService(adapter);
        findUserByIdQuery = new FindUserByIdService(adapter);
    }

    public UserId handle(RegisterNewUserCommand registerNewUserCommand) {
        return registerNewUserUseCase.handle(registerNewUserCommand);
    }

    public User findByUserId(UserId userId) throws UserNotFoundException {
        return findUserByIdQuery.findByUserId(userId);
    }
}
```

