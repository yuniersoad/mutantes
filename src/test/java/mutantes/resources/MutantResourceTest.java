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

    @Before
    public void setUp(){
        reset(ddbTableMock);
        when(ddbTableMock.getItem(any(Subject.class))).thenReturn(completedFuture(null));
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new MutantResource(ddbTableMock))
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
    }

    private Response makeRequest(String[] dna) {
        return resources.target("/mutant")
                .request()
                .post(Entity.entity(new DNARequestPayload(dna), MediaType.APPLICATION_JSON_TYPE));
    }
}