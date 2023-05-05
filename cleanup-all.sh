#!/bin/sh

echo "Clearing screen"
clear

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./cleanup-all.sh AWS_REGION"
  exit 1
fi

echo "Removing old build files"
rm -rf .aws-sam/

echo "Cleaning mvn build"
mvn clean

AWS_REGION=$1

echo "Deleting stack workshop-java-lambda-optimizations"
sam delete --stack-name workshop-java-lambda-optimizations--region $AWS_REGION

