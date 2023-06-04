package com.atn.digital.user;

public class TestLogger implements com.amazonaws.services.lambda.runtime.LambdaLogger {
    @Override
    public void log(String s) {
        System.out.println(s);
    }

    @Override
    public void log(byte[] bytes) {
        System.out.println(new String(bytes));
    }
}
