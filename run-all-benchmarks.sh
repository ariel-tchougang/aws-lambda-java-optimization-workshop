#!/bin/sh

./benchmark.sh plain-java eu-west-1

sleep 30

./benchmark.sh serverless-java-container eu-west-1

sleep 30

./benchmark.sh spring eu-west-1

sleep 30

./benchmark.sh micronaut eu-west-1
