package mutantes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Pattern;

public class DNARequestPayload {
    public static final String DNA_FIELD = "dna";
    private static final Pattern SEQUENCE_REGEX = Pattern.compile("^[a,c,t,g]+$", Pattern.CASE_INSENSITIVE);

    private final String[] dna;

    @JsonCreator
    public DNARequestPayload(@JsonProperty(DNA_FIELD) String[] dna) {
        this.dna = dna;
    }

    @JsonProperty(DNA_FIELD)
    public String[] getDna() {
        return dna;
    }

    public boolean isValid(int maxSize){
        if (dna.length > maxSize)
            return false;
        for (String s: dna) {
            if (s.length() > maxSize || !SEQUENCE_REGEX.matcher(s).matches())
                return false;
        }

        return true;
    }
}
