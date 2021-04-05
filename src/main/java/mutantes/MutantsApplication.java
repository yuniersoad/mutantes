package mutantes;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import mutantes.configuration.DynamoDBConfig;
import mutantes.configuration.RedisConfig;
import mutantes.db.DynamoDBSubjectRepository;
import mutantes.db.RedisCache;
import mutantes.db.SubjectRepository;
import mutantes.health.DynamoDBHealthCheck;
import mutantes.health.RedisHealthCheck;
import mutantes.resources.MutantResource;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.net.URI;

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
        final Pair<DynamoDbAsyncClient, DynamoDbEnhancedAsyncClient> clients = buildDynamoDBclient(configuration);
        final SubjectRepository subjectRepository = new DynamoDBSubjectRepository(clients.getRight());

        final Jedis jedis = buildRedisClient(configuration);
        environment.jersey().register(new MutantResource(subjectRepository, new RedisCache(jedis), configuration.getMaxMatrixSize()));

        environment.healthChecks().register("redis", new RedisHealthCheck(jedis));
        environment.healthChecks().register("dynamodb", new DynamoDBHealthCheck(clients.getLeft()));
    }

    private Jedis buildRedisClient(MutantsConfiguration configuration) {
        final RedisConfig redisConfig = configuration.getRedisConfig();
        return new Jedis(redisConfig.getHost(), redisConfig.getPort(), 100);
    }

    private Pair<DynamoDbAsyncClient, DynamoDbEnhancedAsyncClient> buildDynamoDBclient(MutantsConfiguration configuration) {
        final DynamoDBConfig dbConfig = configuration.getDbConfig();
        final AwsCredentials credentials = AwsBasicCredentials.create(dbConfig.getAccesskeyid(), dbConfig.getSecretkey());
        final DynamoDbAsyncClient client = DynamoDbAsyncClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(dbConfig.getRegion()))
                .endpointOverride(URI.create(dbConfig.getEndpoint()))
                .build();

        final DynamoDbEnhancedAsyncClient enhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(client).build();
        return Pair.of(client, enhancedAsyncClient);
    }
}
