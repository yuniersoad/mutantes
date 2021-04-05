package mutantes.health;

import com.codahale.metrics.health.HealthCheck;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

import java.util.concurrent.ExecutionException;

import static mutantes.db.DynamoDBSubjectRepository.MUTANTS_TABLE;

public class DynamoDBHealthCheck extends HealthCheck {

    private final DynamoDbAsyncClient client;

    public DynamoDBHealthCheck(DynamoDbAsyncClient client) {
        this.client = client;
    }

    @Override
    protected Result check() throws ExecutionException, InterruptedException {
        final DescribeTableResponse describeTableResponse = client.describeTable(DescribeTableRequest.builder().tableName(MUTANTS_TABLE).build()).get();
        if (describeTableResponse.sdkHttpResponse().isSuccessful()){
            return Result.healthy();
        } else {
            return Result.unhealthy("Cannot connect to table: %s", MUTANTS_TABLE);
        }
    }
}
