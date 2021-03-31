package mutantes.db;


import com.google.common.hash.Hashing;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@DynamoDbBean
public class Subject {
    public static final String MUTANTS_TABLE = "Mutants";
    private String id;
    private List<String> dna;
    private Boolean isMutant;

    public Subject(){} //Required by AWS Dynamodb client

    public Subject(List<String> dna) {
        final StringBuilder allDna = new StringBuilder(dna.size() * dna.size());
        for (String s : dna) {
            allDna.append(s);
        }
        this.id = Hashing.sha256()
                .hashString(allDna.toString(), StandardCharsets.UTF_8)
                .toString();
        this.dna = dna;
    }

    @DynamoDbPartitionKey()
    @DynamoDbAttribute("Id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getDna() {
        return dna;
    }

    public void setDna(List<String> dna) {
        this.dna = dna;
    }

    public Boolean getMutant() {
        return isMutant;
    }

    public void setMutant(Boolean mutant) {
        isMutant = mutant;
    }

   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return id.equals(subject.id) && dna.equals(subject.dna);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dna);
    }
}

