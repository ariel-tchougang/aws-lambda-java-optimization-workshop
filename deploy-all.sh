#!/bin/sh

echo "Clearing screen"
clear

if [ "$#" -ne 2 ]; then
  echo "Error: This script requires exactly 2 arguments."
  echo "Usage: ./deploy-all.sh UPLOAD_BUCKET AWS_REGION"
  exit 1
fi

echo "Removing old build files"
rm -rf .workshop-packages
mkdir .workshop-packages

echo "Packaging workshop applications"
mvn clean package
cp ./*/target/*-aws.jar ./.workshop-packages

echo "Packaging sam application"
sam package --s3-bucket $UPLOAD_BUCKET --region $AWS_REGION

UPLOAD_BUCKET=$1
AWS_REGION=$2

echo "Deploying standard version"
sam deploy --no-confirm-changeset --s3-bucket $UPLOAD_BUCKET --region $AWS_REGION

