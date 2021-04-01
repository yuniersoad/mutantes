package mutantes.db;

import mutantes.core.Subject;
import mutantes.db.DynamoDBSubjectRepository.SubjectDynamoDB;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DynamoDBSubjectRepositoryTest {

    public static DynamoDBSubjectRepository repo;
    public static DynamoDbAsyncTable<SubjectDynamoDB> table = mock(DynamoDbAsyncTable.class);
    public static final DynamoDbEnhancedAsyncClient client = mock(DynamoDbEnhancedAsyncClient.class);

    private final String dnaId = "a65d94c3e61fa21513338bde031f53064ad9cb63d8a9b4514e3869cc67db6d32";
    private String[] dnaSample = new String[]{
            "ATGCGA",
            "CAGTGC",
            "TTATGT",
            "AGAAGG",
            "CCCCTA",
            "TCACTG"
    };

    @BeforeClass
    public static void setUp(){
        reset(table);
        when(client.<SubjectDynamoDB>table(anyString(), any())).thenReturn(table);
        repo = new DynamoDBSubjectRepository(client);
    }

    @Test
    public void testFindNothing() throws ExecutionException, InterruptedException {
        when(table.getItem(any(SubjectDynamoDB.class))).thenReturn(completedFuture(null));
        final CompletionStage<Optional<Subject>> subjectO = repo.find(dnaSample);
        assertEquals(Optional.empty(), subjectO.toCompletableFuture().get());
    }

    @Test
    public void testFindSubject() throws ExecutionException, InterruptedException {
        SubjectDynamoDB result = new SubjectDynamoDB(Arrays.asList(dnaSample), true);
        when(table.getItem(any(SubjectDynamoDB.class))).thenReturn(completedFuture(result));
        final CompletionStage<Optional<Subject>> subjectO = repo.find(dnaSample);
        assertEquals(new Subject(Arrays.asList(dnaSample), true), subjectO.toCompletableFuture().get().get());
    }

    @Test
    public void testSave(){
        when(table.putItem(any(SubjectDynamoDB.class))).thenReturn(completedFuture(null));
        repo.save(new Subject(Arrays.asList(dnaSample), true));
        ArgumentCaptor<SubjectDynamoDB> captor = ArgumentCaptor.forClass(SubjectDynamoDB.class);
        verify(table, times(1)).putItem(captor.capture());
        final SubjectDynamoDB valueSaved = captor.getValue();
        assertTrue(valueSaved.getMutant());
        assertEquals(dnaId, valueSaved.getId());
        assertEquals(Arrays.asList(dnaSample), valueSaved.getDna());
    }

    @Test
    public void testGetId() {
        final SubjectDynamoDB s = new SubjectDynamoDB(Arrays.asList(dnaSample), true);
        assertEquals(dnaId, s.getId());
    }

}