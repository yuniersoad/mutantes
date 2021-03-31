package mutantes.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatsResponse {
    public static final String MUTANT_COUNT_FIELD = "count_mutant_dna";
    public static final String HUMAN_COUNT_FIELD = "count_human_dna";
    public static final String RATIO_FIELD = "ratio";

    private final long mutantCount;
    private final long humanCount;
    private final double ratio;

    @JsonCreator
    public StatsResponse(@JsonProperty(MUTANT_COUNT_FIELD) String mutantCount,
                         @JsonProperty(HUMAN_COUNT_FIELD) String humanCount) {
        this.mutantCount = parseOrDefault(mutantCount);
        this.humanCount = parseOrDefault(humanCount);
        if (this.mutantCount == 0 && this.humanCount == 0){
            ratio = 0;
            return;
        }
        ratio = (this.humanCount == 0L) ? Double.POSITIVE_INFINITY : (double) this.mutantCount / this.humanCount;
    }

    private long parseOrDefault(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e){
           return 0;
        }
    }

    @JsonProperty(MUTANT_COUNT_FIELD)
    public long getMutantCount() {
        return mutantCount;
    }

    @JsonProperty(HUMAN_COUNT_FIELD)
    public long getHumanCount() {
        return humanCount;
    }

    @JsonProperty(RATIO_FIELD)
    public double getRatio() {
        return ratio;
    }
}
