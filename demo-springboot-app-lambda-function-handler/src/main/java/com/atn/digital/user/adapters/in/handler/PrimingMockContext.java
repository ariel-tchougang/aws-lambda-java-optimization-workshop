package com.atn.digital.user.adapters.in.handler;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

class PrimingMockContext implements Context {
    public String getAwsRequestId() { return null; }

    public String getLogGroupName() { return null; }

    public String getLogStreamName() { return null; }

    public String getFunctionName() { return null; }

    public String getFunctionVersion() { return null; }

    public String getInvokedFunctionArn() { return null; }

    public CognitoIdentity getIdentity() { return null; }

    public ClientContext getClientContext() { return null; }

    public int getRemainingTimeInMillis() { return 0; }

    public int getMemoryLimitInMB() { return 0; }

    public LambdaLogger getLogger() { return new PrimingMockLogger(); }
}
