package mutantes.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import mutantes.api.StatsResponse;
import mutantes.db.Subject;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.Arrays;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MutantResourceTest {
    public static final DynamoDbAsyncTable<Subject> ddbTableMock = mock(DynamoDbAsyncTable.class);
    public static final Jedis cacheMock = mock(Jedis.class);

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

    @Test
    public void checkStoredMutant() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        Subject storedSubject = new Subject(Arrays.asList(dna));
        storedSubject.setMutant(true);
        when(ddbTableMock.getItem(any(Subject.class))).thenReturn(completedFuture(storedSubject));

        final Response response = makeRequest(dna);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final DNAResponse dnaResponse = response.readEntity(DNAResponse.class);
        assertTrue(dnaResponse.isMutant());

        verify(ddbTableMock, never()).putItem(any(Subject.class));
        verify(cacheMock, never()).incr(anyString());
    }

    @Test
    public void checkStatsSuccess(){
        mockStatsState("100".getBytes(), "40".getBytes());

        final Response response = getStats();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final StatsResponse statsResponse = response.readEntity(StatsResponse.class);
        assertEquals(100L, statsResponse.getHumanCount());
        assertEquals(40L, statsResponse.getMutantCount());
        assertEquals(0.4, statsResponse.getRatio(), 0.000001);
    }

    @Test
    public void checkStatsNotInitialized(){
        // When no stats are yet recorded should be handled successfully
        mockStatsState(null, null);

        final Response response = getStats();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final StatsResponse statsResponse = response.readEntity(StatsResponse.class);
        assertEquals(0, statsResponse.getHumanCount());
        assertEquals(0, statsResponse.getMutantCount());
    }

    private void mockStatsState(byte[] humans, byte[] mutants) {
        Pipeline pMock = mock(Pipeline.class);
        when(cacheMock.pipelined()).thenReturn(pMock);
        final redis.clients.jedis.Response humanCountResponse = new redis.clients.jedis.Response(BuilderFactory.STRING);
        humanCountResponse.set(humans);
        final redis.clients.jedis.Response mutantCountResponse = new redis.clients.jedis.Response(BuilderFactory.STRING);
        mutantCountResponse.set(mutants);
        when(pMock.get(eq(MutantResource.HUMAN_COUNT_CACHE_KEY))).thenReturn(humanCountResponse);
        when(pMock.get(eq(MutantResource.MUTANT_COUNT_CACHE_KEY))).thenReturn(mutantCountResponse);
    }

    private Response getStats() {
        final Response response = resources.target("/stats").request().get();
        return response;
    }

    private Response makeRequest(String[] dna) {
        return resources.target("/mutant")
                .request()
                .post(Entity.entity(new DNARequestPayload(dna), MediaType.APPLICATION_JSON_TYPE));
    }
}