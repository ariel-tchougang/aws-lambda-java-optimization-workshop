#!/bin/sh

echo "Clearing screen"
clear

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./redeploy-all.sh AWS_REGION"
  exit 1
fi

AWS_REGION=$1

echo "Packaging sam application"
sam package --region $AWS_REGION

echo "Deploying standard version"
sam deploy --region $AWS_REGION

