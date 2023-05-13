#!/bin/sh

if [ "$#" -ne 2 ]; then
  echo "Error: This script requires exactly 2 arguments."
  echo "Usage: ./benchmark.sh TARGET_APP AWS_REGION"
  echo "TARGET_APP in ['plain-java', 'serverless-java-container', 'spring', 'micronaut'"
  echo "Example: ./benchmark.sh plain-java eu-west-1"
  exit 1
fi

APP=$1
AWS_REGION=$2

STACK_NAME="workshop-java-lambda-optimizations"
API_GW_URL=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[0].Outputs[?OutputKey=='WorkshopApiUrl'].OutputValue" --region $AWS_REGION --output text)

if [ $APP == "plain-java" ]; then
  echo "Running Artillery on plain-java-request-handler"
  artillery run -t $API_GW_URL loadtest/plain-java-request-handler.yaml
  sleep 30
  exit 0
fi

if [ $APP == "serverless-java-container" ]; then
  echo "Running Artillery on serverless-java-container"
  artillery run -t $API_GW_URL loadtest/serverless-java-container.yaml
  sleep 30
  exit 0
fi

if [ $APP == "spring" ]; then
  echo "Running Artillery on springboot-function-handler"
  artillery run -t $API_GW_URL loadtest/springboot-function-handler.yaml
  sleep 30
  exit 0
fi

if [ $APP == "micronaut" ]; then
  echo "Running Artillery on micronaut-request-handler"
  artillery run -t $API_GW_URL loadtest/micronaut-request-handler.yaml
  sleep 30
  exit 0
fi

echo "Usage: ./benchmark.sh TARGET_APP AWS_REGION"
echo "TARGET_APP in ['plain-java', 'serverless-java-container', 'spring', 'micronaut'"
echo "Example: ./benchmark.sh plain-java eu-west-1"
exit 1

