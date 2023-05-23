package com.atn.digital.user.adapters.in.handler;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.ApiGatewayRequestIdentity;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.atn.digital.user.UserApplication;

import org.crac.Core;
import org.crac.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

public class StreamLambdaHandlerWithPriming implements RequestStreamHandler, Resource {

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
    
    public StreamLambdaHandlerWithPriming () {
		Core.getGlobalContext().register(this);
	}

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
    
    @Override
	public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
		System.out.println("Before Checkpoint");
	    handler.proxy(getAwsProxyRequest(), new MockLambdaContext());
	    System.out.println("After Checkpoint");
	}

	@Override
	public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {
		System.out.println("After Restore");	
	}
	
	private static AwsProxyRequest getAwsProxyRequest () {
    	final AwsProxyRequest awsProxyRequest = new AwsProxyRequest ();
    	awsProxyRequest.setHttpMethod("GET");
    	awsProxyRequest.setPath("/serverless-java-container/users/id");
    	awsProxyRequest.setResource("/serverless-java-container/users/{userId}");
    	awsProxyRequest.setPathParameters(Map.of("userId", UUID.randomUUID().toString()));
    	final AwsProxyRequestContext awsProxyRequestContext = new AwsProxyRequestContext();
    	final ApiGatewayRequestIdentity apiGatewayRequestIdentity= new ApiGatewayRequestIdentity();
    	apiGatewayRequestIdentity.setApiKey("loremIpsum");
    	awsProxyRequestContext.setIdentity(apiGatewayRequestIdentity);
    	awsProxyRequest.setRequestContext(awsProxyRequestContext);
    	return awsProxyRequest;		
    }
}
