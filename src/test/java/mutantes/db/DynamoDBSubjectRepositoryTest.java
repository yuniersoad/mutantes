package mutantes.db;

import mutantes.core.Subject;
import mutantes.db.DynamoDBSubjectRepository.SubjectDynamoDB;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
        when(client.<SubjectDynamoDB>table(anyString(), any())).thenReturn(table);
        repo = new DynamoDBSubjectRepository(client);
    }

    @Before
    public void before(){
        reset(table);
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
        ArgumentCaptor<SubjectDynamoDB> captor = ArgumentCaptor.forClass(SubjectDynamoDB.class);
        verify(table, times(1)).getItem(captor.capture());
        final String searchedKey = captor.getValue().getId();
        assertEquals(dnaId, searchedKey);
        assertTrue(subjectO.toCompletableFuture().get().get().getMutant());
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
    }

    @Test
    public void testGeneratedId() {
        final SubjectDynamoDB s = new SubjectDynamoDB(Arrays.asList(dnaSample), true);
        assertEquals(dnaId, s.getId());
    }

    @Test
    public void testGeneratedIdIsCaseInsensitive() {
        final List<String> dnaSampleDiffCase = Arrays.stream(dnaSample).map(String::toLowerCase).collect(Collectors.toList());
        final SubjectDynamoDB s = new SubjectDynamoDB(dnaSampleDiffCase, true);
        assertEquals(dnaId, s.getId());
    }

}