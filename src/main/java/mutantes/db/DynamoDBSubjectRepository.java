package mutantes.db;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import mutantes.core.Subject;
import mutantes.resources.MutantResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DynamoDBSubjectRepository implements SubjectRepository {
    private final static Logger log = LoggerFactory.getLogger(MutantResource.class);

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
                return Optional.of(s.toCore(dna));
            return Optional.<Subject>empty();
        }).exceptionally(throwable -> {
            // Just log DB errors so we can detect the issue, but keep processing the request since mutant detection is still possible
            log.error("Error fetching record: ", throwable);
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
        private Boolean isMutant;
        private ZonedDateTime dateAdded;


        public SubjectDynamoDB(){} //Required by AWS Dynamodb client

        public SubjectDynamoDB(List<String> dna, Boolean isMutant) {
            Hasher hashCode = Hashing.sha256().newHasher();
            for (String s : dna) {
                hashCode = hashCode.putString(s.toUpperCase(), StandardCharsets.UTF_8);

            }
            this.id = hashCode.hash().toString();
            this.isMutant = isMutant;
            this.dateAdded = ZonedDateTime.now();
        }

        public SubjectDynamoDB(Subject subject){
            this(subject.getDna(), subject.getMutant());
        }

        @DynamoDbPartitionKey()
        @DynamoDbAttribute("Id")
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Boolean getMutant() {
            return isMutant;
        }

        public void setMutant(Boolean mutant) {
            isMutant = mutant;
        }

        public ZonedDateTime getDateAdded() {
            return dateAdded;
        }

        public void setDateAdded(ZonedDateTime dateAdded) {
            this.dateAdded = dateAdded;
        }

        @DynamoDbIgnore
        public Subject toCore(String[] dna){
            return new Subject(Arrays.asList(dna), isMutant);
        }
    }
}
