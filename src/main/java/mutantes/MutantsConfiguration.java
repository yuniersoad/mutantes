package mutantes;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import mutantes.configuration.DynamoDBConfig;
import mutantes.configuration.RedisConfig;


public class MutantsConfiguration extends Configuration {
    private DynamoDBConfig dbConfig;
    private RedisConfig  redisConfig;
    private Integer maxMatrixSize;

    @JsonProperty("DBConfig")
    public DynamoDBConfig getDbConfig() {
        return dbConfig;
    }

    @JsonProperty("RedisConfig")
    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    @JsonProperty("maxMatrixSize")
    public Integer getMaxMatrixSize() {
        return maxMatrixSize;
    }
}
