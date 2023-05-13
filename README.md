# Demo Java optimization on AWS Lambda
> Using *Hexagonal (Ports & Adapters) Architecture & TDD*

## Technologies used:
* Java 17
* AWS Lambda
* Amazon API Gateway
* Spring boot 2.6.14
* Micronaut 3.9.1
* Lombok
* Maven
* DynamoDB Local (docker container amazon/dynamodb-local:latest)
* org.testcontainers
* Junit 5
* npm
* jq  (On Amazon Linux 2 run: 'sudo yum install jq' )
* Artillery  (On Amazon Linux 2 run: 'npm install -g artillery@latest' )
* faker-js
* AWS SAM CLI

## About SAM CLI

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda. It can also emulate your application's build environment and API.

To use the SAM CLI, you need the following tools.

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java 17 - [Install the Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Docker - [Install Docker community edition](https://hub.docker.com/search/?type=edition&offering=community)

## Content
This application is a user microservice exposing REST endpoints for:
* Registering a new user, 
* Fetching existing user details

A simple Springboot application version (demo-springboot-app) is provided as starting point. 
A corresponding initial architecture could be the following:

![Alt text](/images/Workshop-Initial-Architecture.png?raw=true "Initial architecture")

### Code inside architecture

![Alt text](/images/Code-Architecture.png?raw=true "Code architecture")

## General purpose and target architecture
The purpose of this project is to compare the performance of this application deployed as a AWS Lambda function with the following targets:
* Springboot with serverless java container
* Springboot with lambda function handler
* Micronaut with lambda request handler
* Plain Java with lambda handler

The target architecture is the following:

![Alt text](/images/Workshop-Target-Architecture.png?raw=true "Target architecture")

### Code inside architecture

![Alt text](/images/Code-Architecture-for-Lambda.png?raw=true "Code architecture")

### Initial configuration

For our AWS Lambda function, the default configuration is as following:

* Memory size: 256 MB
* Tiered compilation activated: -XX:+TieredCompilation -XX:TieredStopAtLevel=1
* Architecture: x86_64

### Converting the initial Spring boot app to AWS Lambda with serverless java container
#### Add the AWS Serverless Java Container library

```xml
<dependency>
    <groupId>com.amazonaws.serverless</groupId>
    <artifactId>aws-serverless-java-container-springboot2</artifactId>
    <version>1.9.3</version>
</dependency>
```
#### Excluding Embedded Tomcat Server

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.4.1</version>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <artifactSet>
                            <excludes>
                                <exclude>org.apache.tomcat.embed:*</exclude>
                            </excludes>
                        </artifactSet>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

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

### AWS Lambda using Plain Request Handler

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

## How will we proceed for workshop

For each of the listed target, we will:
* Build the application
* Deploy on AWS Lambda
* Assess initial performances
* Apply [Lambda Power Tuning Tool](https://github.com/alexcasalboni/aws-lambda-power-tuning) to find the right memory configuration
* Redeploy with memory optimization
* Assess performances
* Apply Java Snapstart for AWS Lambda
* Redeploy with Snapstart for AWS Lambda
* Assess performances

## Let's get started

### Make all scripts executable

```bash
chmod 755 *.sh
```

### Build and deploy the application on AWS Lambda

```bash
./deploy-all.sh UPLOAD_BUCKET AWS_REGION
```


### Install Artillery, jq & faker-js

* Artillery
```bash
echo "Installing Artillery"
npm install -g artillery@latest 

echo "Testing installation"
artillery dino
```

* jq (On Amazon Linux 2)
```bash
sudo yum install jq
```

* faker-js (only if needed, because generated node_modules resources are already provided in folder loadtest)
```bash
cd loadtest
npm init -y && npm install @faker-js/faker
cd ..
```

#### Deployment information

```
stack_name = "workshop-java-lambda-optimizations"
s3_prefix = "workshop-java-lambda-optimizations"

Lambda functions: 
    - workshop-springboot-serverless-java-container
    - workshop-springboot-lambda-function-handler
    - workshop-micronaut-lambda-request-handler
    - workshop-plain-java-lambda-request-handler
```

### Assessing initial performances

#### Testing each lambda once to have a first feeling of performance
```bash
./test-app.sh TARGET_APP AWS_REGION
```

* With TARGET_APP values in ['plain-java', 'serverless-java-container', 'spring', 'micronaut'

Result will look like:
![Alt text](/images/test-app-example-01.png?raw=true "Testing app")

#### Run a load test benchmark with Artillery

Load test configuration files are stored in folder loadtest.

You can run a benchmark per target (may be in parallel bash windows - by default 60 seconds per benchmark)
```bash
./benchmark.sh TARGET_APP AWS_REGION
```
* With TARGET_APP values in ['plain-java', 'serverless-java-container', 'spring', 'micronaut'

OR

You can run your benchmarks all at once.
```bash
./run-all-benchmarks.sh AWS_REGION
```

#### Visualize your results
Then go to CloudWatch Logs Insights:

* Select the appropriate log groups
    - /aws/lambda/workshop-springboot-serverless-java-container
    - /aws/lambda/workshop-springboot-lambda-function-handler
    - /aws/lambda/workshop-micronaut-lambda-request-handler
    - /aws/lambda/workshop-plain-java-lambda-request-handler
* Collect your logs using this query:

```sql
  filter @type = "REPORT"
  | parse @log /\d+:\/aws\/lambda\/(?<function>.*)/
  | parse @message /Restore Duration: (?<restoreDuration>.*?) ms/
  | stats
count(*) as invocations,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 0) as p0,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 25) as p25,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 50) as p50,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 90) as p90,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 95) as p95,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 99) as p99,
pct(@duration+coalesce(@initDuration,0)+coalesce(restoreDuration,0), 100) as p100
group by function, (ispresent(@initDuration) or ispresent(restoreDuration)) as coldstart
  | sort by coldstart desc

```

Result will look like:
![Alt text](/images/Benchmark%20-%2001%20-%20Initial%20values.png?raw=true "Initial benchmark")

### Install [Lambda Power Tuning Tool](https://github.com/alexcasalboni/aws-lambda-power-tuning)
Execute the state machine to find the right memory configuration. The execution payloads are in folder payloads.
We'll be looking for the best memory size with an acceptable cost.

#### Execution payload template

```json
{
  "lambdaARN": "REPLACE_WITH_TARGET_LAMBDA_ARN",
  "powerValues": [
    512,
    1024,
    1536,
    2048,
    2560,
    3072,
    4096
  ],
  "parallelInvocation": true,
  "num": 5,
  "payload": {
    "resource": "/{proxy+}",
    "path": "/REPLACE_WITH_ROOT_PATH/users",
    "httpMethod": "POST",
    "multiValueHeaders": {
      "content-type": [
        "application/json"
      ]
    },
    "pathParameters": {
      "proxy": "REPLACE_WITH_ROOT_PATH/users"
    },
    "requestContext": {
      "identity": {
        "userAgent": "curl/7.64.1"
      }
    },
    "body": "{\n \"firstName\": \"user\",\n \"lastName\": \"testing\",\n \"email\": \"user.testing@workshop.com\"}",
    "isBase64Encoded": false
  }
}
```

Results will look like:
* For Lambda with Serverless Java Container
![Alt text](/images/Memory-Phase-02-01-Plain-Java.png?raw=true "Serverless Java Container")


* For Lambda with Spring Cloud Function
![Alt text](/images/Memory-Phase-02-02-Springboot-With-Serverless-Java-Container.png?raw=true "Spring Cloud Function")


* For Lambda with Micronaut
![Alt text](/images/Memory-Phase-02-03-Springboot-With-Spring-Cloud-Function-Handler.png?raw=true "Micronaut")

* For Lambda with Plain java
![Alt text](images/Memory-Phase-02-04-Micronaut-With-Lambda-Request-Handler.png?raw=true "Plain java")

### Redeploy with memory optimization

Update template.yaml to reflect your optimal memory choices.

* Global values
````yaml
Globals:
  Function:
    Timeout: 30
    Runtime: java17
    # MemorySize: 512
````

* For Serverless Java Container

```yaml
  SpringbootServerlessJavaContainer:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: .workshop-packages/demo-springboot-app-lambda-serverless-java-container-1.0.0-aws.jar
      FunctionName: workshop-springboot-serverless-java-container
      Handler: com.atn.digital.user.StreamLambdaHandler::handleRequest
      MemorySize: 3072 #SET NEW VALUE HERE
```

* For Spring Cloud Function

```yaml
  SpringbootLambdaFunctionHandler:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: .workshop-packages/demo-springboot-app-lambda-function-handler-1.0.0-aws.jar
      FunctionName: workshop-springboot-lambda-function-handler
      Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
      MemorySize: 2048 #SET NEW VALUE HERE
```

* For Micronaut

```yaml
  MicronautLambdaRequestHandler:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: .workshop-packages/demo-micronaut-app-lambda-request-handler-1.0.0-aws.jar
      FunctionName: workshop-micronaut-lambda-request-handler
      Handler: com.atn.digital.user.adapters.in.handler.UserRequestHandler::handleRequest
      MemorySize: 2560 #SET NEW VALUE HERE
```

* For Plain java

```yaml
  PlainJavaLambdaRequestHandler:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: .workshop-packages/demo-plain-java-app-lambda-handler-1.0.0-aws.jar
      FunctionName: workshop-plain-java-lambda-request-handler
      Handler: com.atn.digital.user.adapters.in.handler.UserRequestHandler::handleRequest
      MemorySize: 2560 #SET NEW VALUE HERE
```

#### Redeploy

* If you changed something pertaining to your java code:
```bash
./deploy-all.sh UPLOAD_BUCKET AWS_REGION
```

* If you didn't make any changes pertaining to your java code
```bash
./redeploy-all.sh UPLOAD_BUCKET AWS_REGION
```

#### Testing each lambda once to have a first feeling of performance
```bash
./test-app.sh TARGET_APP AWS_REGION
```

Result will look like:
![Alt text](/images/test-app-example-02.png?raw=true "Testing app")

### Run a load test benchmark with Artillery

Load test configuration files are stored in folder loadtest.

* Go to Cloudwatch 
* Delete all the workshop function log groups
* Run you benchmark

```bash
./benchmark.sh TARGET_APP AWS_REGION
```
OR

```bash
./run-all-benchmarks.sh
```

#### Visualize your results
* Then go to CloudWatch Logs Insights
* Select the appropriate log groups
* Collect your logs using the same query

Result will look like:
![Alt text](/images/Benchmark%20-%2002%20-%20After%20applying%20optimal%20memory%20size.png?raw=true "After memory optimization benchmark")

### Apply Java SnapStart for AWS Lambda

Update template.yaml to enable SnapStart.

* Global values
````yaml
Globals:
  Function:
    Timeout: 30
    Runtime: java17
    # MemorySize: 512
    SnapStart:
      ApplyOn: PublishedVersions
````

#### Redeploy

* If you changed something pertaining to your java code:
```bash
./deploy-all.sh UPLOAD_BUCKET AWS_REGION
```

* If you didn't make any changes pertaining to your java code
```bash
./redeploy-all.sh UPLOAD_BUCKET AWS_REGION
```

#### Testing each lambda once to have a first feeling of performance
```bash
./test-app.sh TARGET_APP AWS_REGION
```

Result will look like:
![Alt text](/images/test-app-example-03.png?raw=true "Testing app")

### Run a load test benchmark with Artillery

Load test configuration files are stored in folder loadtest.

* Go to Cloudwatch 
* Delete all the workshop function log groups
* Run you benchmark

```bash
./benchmark.sh TARGET_APP AWS_REGION
```
OR

```bash
./run-all-benchmarks.sh
```

#### Visualize your results
* Then go to CloudWatch Logs Insights
* Select the appropriate log groups
* Collect your logs using the same query

Result will look like:
![Alt text](/images/Benchmark%20-%2003%20-%20After%20applying%20Snapstart.png?raw=true "After memory optimization benchmark")

## Cleanup
````bash
./cleanup-all.sh AWS_REGION
````

## Resources

See the [AWS SAM developer guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html) for an introduction to SAM specification, the SAM CLI, and serverless application concepts.

Next, you can use AWS Serverless Application Repository to deploy ready to use Apps that go beyond hello world samples and learn how authors developed their applications: [AWS Serverless Application Repository main page](https://aws.amazon.com/serverless/serverlessrepo/).

Official AWS workshop on [Java on AWS Lambda](https://catalog.workshops.aws/java-on-aws-lambda/en-US).
