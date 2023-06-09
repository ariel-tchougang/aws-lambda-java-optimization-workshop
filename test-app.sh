#bin/sh

if [ "$#" -ne 2 ]; then
  echo "Error: This script requires exactly 2 arguments."
  echo "Usage: ./test-app.sh TARGET_APP AWS_REGION"
  echo "TARGET_APP in ['plain-java', 'serverless-java-container', 'spring', 'micronaut', 'quarkus'"
  echo "Example: ./test-app.sh plain-java eu-west-1"
  exit 1
fi

APP=$1
AWS_REGION=$2
STACK_NAME="workshop-java-lambda-optimizations"
API_GW_URL=$(aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[0].Outputs[?OutputKey=='WorkshopApiUrl'].OutputValue" --region $AWS_REGION --output text)

if [ $APP == "plain-java" ]; then
  curl --location --request POST $API_GW_URL'/Prod/plain-java-request-handler/users' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "firstName": "Plain",
    "lastName": "Java",
    "email": "plain.java@workshop.demo"
}' | jq

  exit 0
fi

if [ $APP == "serverless-java-container" ]; then
  curl --location --request POST $API_GW_URL'/Prod/serverless-java-container/users' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "firstName": "Serverless",
    "lastName": "Java-Container",
    "email": "serverless.java.container@workshop.demo"
}' | jq

  exit 0
fi


if [ $APP == "spring" ]; then
    curl --location --request POST $API_GW_URL'/Prod/springboot-function-handler/users' \
      --header 'Content-Type: application/json' \
      --data-raw '{
        "firstName": "Springboot",
        "lastName": "Function-Handler",
        "email": "spring@workshop.demo"
    }' | jq

  exit 0
fi

if [ $APP == "micronaut" ]; then
    curl --location --request POST $API_GW_URL'/Prod/micronaut-request-handler/users' \
      --header 'Content-Type: application/json' \
      --data-raw '{
        "firstName": "Micronaut",
        "lastName": "Request-Handler",
        "email": "micronaut@workshop.demo"
    }' | jq

  exit 0
fi

if [ $APP == "quarkus" ]; then
    curl --location --request POST $API_GW_URL'/Prod/quarkus-request-handler/users' \
      --header 'Content-Type: application/json' \
      --data-raw '{
        "firstName": "Quarkus",
        "lastName": "Request-Handler",
        "email": "quarkus@workshop.demo"
    }' | jq

  exit 0
fi

echo "Error: This script requires exactly 2 arguments."
echo "Usage: ./benchmark.sh TARGET_APP AWS_REGION"
echo "TARGET_APP in ['plain-java', 'serverless-java-container', 'spring', 'micronaut', 'quarkus'"
echo "Example: ./test-app.sh plain-java eu-west-1"
exit 1
