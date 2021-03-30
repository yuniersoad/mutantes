package mutantes.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import mutantes.db.Subject;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import redis.clients.jedis.commands.JedisCommands;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MutantResourceTest {
    public static final DynamoDbAsyncTable<Subject> ddbTableMock = mock(DynamoDbAsyncTable.class);
    public static final JedisCommands cacheMock = mock(JedisCommands.class);

    @Before
    public void setUp(){
        reset(ddbTableMock);
        reset(cacheMock);
        when(ddbTableMock.getItem(any(Subject.class))).thenReturn(completedFuture(null));
        when(ddbTableMock.putItem(any(Subject.class))).thenReturn(completedFuture(null));
        when(cacheMock.incr(anyString())).thenReturn(1L);
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new MutantResource(ddbTableMock, cacheMock))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .build();

    @Test
    public void checkMutantPositive() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        final Response response = makeRequest(dna);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final DNAResponse dnaResponse = response.readEntity(DNAResponse.class);
        assertTrue(dnaResponse.isMutant());

        ArgumentCaptor<Subject> subjectSavedCaptor = ArgumentCaptor.forClass(Subject.class);
        verify(ddbTableMock).putItem(subjectSavedCaptor.capture());
        Subject subjectSaved = subjectSavedCaptor.getValue();
        assertTrue(subjectSaved.getMutant());
        assertArrayEquals(dna, subjectSaved.getDna().toArray());
        verify(cacheMock, times(1)).incr(MutantResource.MUTANT_COUNT_CACHE_KEY);
    }


    @Test
    public void checkMutantNevagitve(){
        final Response response = makeRequest(new String[] {
                "ATGCGA",
                "CAGTGC",
                "TTATTT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
        });
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        final DNAResponse dnaResponse = response.readEntity(DNAResponse.class);
        assertFalse(dnaResponse.isMutant());

        ArgumentCaptor<Subject> subjectSavedCaptor = ArgumentCaptor.forClass(Subject.class);
        verify(ddbTableMock).putItem(subjectSavedCaptor.capture());
        Subject subjectSaved = subjectSavedCaptor.getValue();
        assertFalse(subjectSaved.getMutant());
        verify(cacheMock, times(1)).incr(MutantResource.HUMAN_COUNT_CACHE_KEY);
    }

    private Response makeRequest(String[] dna) {
        return resources.target("/mutant")
                .request()
                .post(Entity.entity(new DNARequestPayload(dna), MediaType.APPLICATION_JSON_TYPE));
    }
}