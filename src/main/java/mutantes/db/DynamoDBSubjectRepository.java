package mutantes.db;

import com.google.common.hash.Hashing;
import mutantes.core.Subject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DynamoDBSubjectRepository implements SubjectRepository {
    public static final String MUTANTS_TABLE = "Mutants";

    private final DynamoDbAsyncTable<SubjectDynamoDB> table;

    public DynamoDBSubjectRepository(DynamoDbEnhancedAsyncClient ddbclient) {
        this.table = ddbclient.table(DynamoDBSubjectRepository.MUTANTS_TABLE, TableSchema.fromBean(DynamoDBSubjectRepository.SubjectDynamoDB.class));
    }

    @Override
    public CompletionStage<Optional<Subject>> find(String[] dna) {
        final SubjectDynamoDB subject = new SubjectDynamoDB(Arrays.asList(dna), false);
        return table.getItem(subject).thenApply(s -> {
            if (s != null)
                return Optional.of(subject.toCore());
            return Optional.empty();
        });
    }

    @Override
    public CompletionStage<Void> save(Subject subject) {
        return table.putItem(new SubjectDynamoDB(subject));
    }

    @DynamoDbBean
    public static class SubjectDynamoDB {
        private String id;
        private List<String> dna;
        private Boolean isMutant;

        public SubjectDynamoDB(){} //Required by AWS Dynamodb client

        public SubjectDynamoDB(List<String> dna, Boolean isMutant) {
            final StringBuilder allDna = new StringBuilder(dna.size() * dna.size());
            for (String s : dna) {
                allDna.append(s);
            }
            this.id = Hashing.sha256()
                    .hashString(allDna.toString(), StandardCharsets.UTF_8)
                    .toString();
            this.dna = dna;
            this.isMutant = isMutant;
        }

        public SubjectDynamoDB(Subject subject){
            this(subject.getDna(), subject.getMutant());
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

        @DynamoDbIgnore
        public Subject toCore(){
            return new Subject(dna, isMutant);
        }

    }
}
