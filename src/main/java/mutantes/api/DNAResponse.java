package mutantes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DNAResponse {
    public static final String IS_MUTANT_FIELD = "is_mutant";

    private boolean isMutant;

    @JsonCreator
    public DNAResponse(@JsonProperty(IS_MUTANT_FIELD) final boolean isMutant) {
        this.isMutant = isMutant;
    }

    @JsonProperty(IS_MUTANT_FIELD)
    public boolean isMutant() {
        return isMutant;
    }
}
