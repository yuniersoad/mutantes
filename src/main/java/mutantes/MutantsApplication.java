package mutantes;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import mutantes.configuration.DynamoDBConfig;
import mutantes.configuration.RedisConfig;
import mutantes.db.Subject;
import mutantes.resources.MutantResource;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class MutantsApplication extends Application<MutantsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MutantsApplication().run(args);
    }

    @Override
    public String getName() {
        return "mutants";
    }

    @Override
    public void initialize(final Bootstrap<MutantsConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
    }

    @Override
    public void run(final MutantsConfiguration configuration,
                    final Environment environment) {
        final DynamoDbEnhancedAsyncClient ddbclient = buildDynamoDBclient(configuration);

        final Jedis jedis = buildRedisClient(configuration);
        environment.jersey().register(new MutantResource(ddbclient.table(Subject.MUTANTS_TABLE, TableSchema.fromBean(Subject.class)), jedis));
    }

    private Jedis buildRedisClient(MutantsConfiguration configuration) {
        final RedisConfig redisConfig = configuration.getRedisConfig();
         return new Jedis(redisConfig.getHost(), redisConfig.getPort(), 100);
    }

    private DynamoDbEnhancedAsyncClient buildDynamoDBclient(MutantsConfiguration configuration) {
        final DynamoDBConfig dbConfig = configuration.getDbConfig();
        final AwsCredentials credentials = AwsBasicCredentials.create(dbConfig.getDbaccesskeyid(), dbConfig.getDbsecretkey());
        final DynamoDbAsyncClient client = DynamoDbAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(dbConfig.getDbregion()))
                .build();
        final DynamoDbEnhancedAsyncClient ddbclient = DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(client).build();
        return ddbclient;
    }
}
