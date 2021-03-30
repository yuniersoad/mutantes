package mutantes.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import software.amazon.awssdk.regions.Region;

public class DynamoDBConfig {
    @NotEmpty
    private String dbregion = Region.US_EAST_1.toString();

    @NotEmpty
    private String dbaccesskeyid;

    @NotEmpty
    private String dbsecretkey;

    @JsonProperty("DBRegion")
    public String getDbregion() {
        return dbregion;
    }

    @JsonProperty("DBAccessKeyId")
    public String getDbaccesskeyid() {
        return dbaccesskeyid;
    }

    @JsonProperty("DBSecretKey")
    public String getDbsecretkey() {
        return dbsecretkey;
    }
}
