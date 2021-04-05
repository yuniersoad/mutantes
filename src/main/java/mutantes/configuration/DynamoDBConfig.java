package mutantes.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import software.amazon.awssdk.regions.Region;

public class DynamoDBConfig {
    @NotEmpty
    private String region = Region.US_EAST_1.toString();

    @NotEmpty
    private String endpoint;

    @NotEmpty
    private String accesskeyid;

    @NotEmpty
    private String secretkey;

    @JsonProperty("Region")
    public String getRegion() {
        return region;
    }

    @JsonProperty("AccessKeyId")
    public String getAccesskeyid() {
        return accesskeyid;
    }

    @JsonProperty("SecretKey")
    public String getSecretkey() {
        return secretkey;
    }

    @JsonProperty("Endpoint")
    public String getEndpoint() {
        return endpoint;
    }
}
