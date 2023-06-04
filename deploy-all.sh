#!/bin/sh

echo "Clearing screen"
clear

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./deploy-all.sh AWS_REGION"
  exit 1
fi

echo "Removing old build files"
rm -rf .workshop-packages
mkdir .workshop-packages

echo "Packaging workshop applications"
mvn clean package
cp ./*/target/*-aws.jar ./.workshop-packages

echo "Packaging quarkus app"
cp ./demo-quarkus-app-lambda-request-handler/target/function.zip ./.workshop-packages/
mv ./.workshop-packages/function.zip ./.workshop-packages/demo-quarkus-app-lambda-request-handler.zip

AWS_REGION=$1

echo "Packaging sam application"
sam package --region $AWS_REGION

echo "Deploying standard version"
sam deploy --region $AWS_REGION

