# Demo Java optimization on AWS Lambda
> Using *Hexagonal (Ports & Adapters) Architecture & TDD*

## Content
This application is a user microservice exposing REST endpoints for:
* Registering a new user, 
* Fetching existing user details

A simple Springboot application version (demo-springboot-app) is provided as starting point.
And a corresponding initial architecture could be the following:

![Alt text](/images/Workshop-Initial-Architecture.png?raw=true "Initial architecture")

## General purpose
The purpose of this project is to compare the performance of this application deployed as aa AWS Lambda function with the following targets:
* Springboot with serverless java container
* Springboot with lambda function handler
* Micronaut with lambda request handler
* Plain Java with lambda handler

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
* jq
* Artillery
* AWS SAM CLI

## About SAM CLI

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda. It can also emulate your application's build environment and API.

To use the SAM CLI, you need the following tools.

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java11 - [Install the Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Docker - [Install Docker community edition](https://hub.docker.com/search/?type=edition&offering=community)

## How will we proceed

For each of the listed target, we will:
* Build the application
* Deploy on AWS Lambda
* Assess initial performances
* Apply [Lambda Power Tuning Tool](https://github.com/alexcasalboni/aws-lambda-power-tuning) to find the right memory configuration
* Redeploy with memory optimization
* Assess performances
* Apply JVM Tiered compilation
* Redeploy with JVM Tiered compilation
* Assess performances
* Apply Java Snapstart for AWS Lambda
* Redeploy with Snapstart for AWS Lambda
* Assess performances


## Resources

See the [AWS SAM developer guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html) for an introduction to SAM specification, the SAM CLI, and serverless application concepts.

Next, you can use AWS Serverless Application Repository to deploy ready to use Apps that go beyond hello world samples and learn how authors developed their applications: [AWS Serverless Application Repository main page](https://aws.amazon.com/serverless/serverlessrepo/).

Official AWS workshop on [Java on AWS Lambda](https://catalog.workshops.aws/java-on-aws-lambda/en-US).
