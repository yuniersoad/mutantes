# mutantes

## Requirements
 - Java 8
 - Maven
 - AWS cli (nice to have)

## How to start the mutantes application

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/mutantes-1.0-SNAPSHOT.jar server config.yml`, specify any config as env vars,
   more on that later.
1. The application can be reached at  `http://localhost:8080`

### Dependencies

The following external services are required for the application to be fully functional:

 - **DynamoDB**, used to store the already seen DNA samples (actually only the sha256 is stored).
 - **Redis**, used to keep track of stats. 

More on how to configure those in the next section

### Configuration

The following env vars can be set to config the application

Name | Default
------------ | -------------
AWS_DYNAMODB_ENDPOINT | https://dynamodb.us-east-1.amazonaws.com
AWS_DEFAULT_REGION | us-east-1
AWS_ACCESS_KEY_ID |
AWS_SECRET_ACCESS_KEY |
REDIS_HOST | localhost
REDIS_PORT | 6379
MAX_MATRIX_SIZE | 5000 (~24MB)

## Health Check

To see your applications health enter url `http://localhost:8081/healthcheck`

## Running with Docker compose (Local DB & cache)

`mvn package && docker-compose up --build`

Create the table before making requests with:

`AWS_ACCESS_KEY_ID=OK AWS_SECRET_ACCESS_KEY=OK aws dynamodb create-table 
--endpoint-url http://localhost:8000 --region us-east-1 
--table-name Mutants 
--attribute-definitions AttributeName=Id,AttributeType=S 
--key-schema AttributeName=Id,KeyType=HASH 
--provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1`


Then you can inspect the local DynamoDB with any supported commands like for example:

`AWS_ACCESS_KEY_ID=OK AWS_SECRET_ACCESS_KEY=OK aws dynamodb list-tables 
--endpoint-url http://localhost:8000 --region us-east-1`

`AWS_ACCESS_KEY_ID=OK AWS_SECRET_ACCESS_KEY=OK aws dynamodb scan 
--table-name Mutants --endpoint-url http://localhost:8000 --region us-east-1`

To clean stored values after stopping:

`docker-compose down`
