#!/bin/sh

if [ "$#" -ne 1 ]; then
  echo "Error: This script requires exactly 1 argument."
  echo "Usage: ./run-all-benchmarks.sh AWS_REGION"
  echo "Example: ./run-all-benchmarks.sh eu-west-1"
  exit 1
fi

AWS_REGION=$1

./benchmark.sh plain-java $AWS_REGION &


./benchmark.sh serverless-java-container $AWS_REGION &


./benchmark.sh spring $AWS_REGION &


./benchmark.sh micronaut $AWS_REGION &
