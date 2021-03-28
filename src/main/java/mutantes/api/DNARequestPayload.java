package mutantes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DNARequestPayload {
    public static final String DNA_FIELD = "dna";
    private final String[] dna;

    @JsonCreator
    public DNARequestPayload(@JsonProperty(DNA_FIELD) String[] dna) {
        this.dna = dna;
    }

    @JsonProperty(DNA_FIELD)
    public String[] getDna() {
        return dna;
    }
}
