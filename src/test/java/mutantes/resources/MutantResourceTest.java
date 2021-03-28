package mutantes.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.*;

public class MutantResourceTest {
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new MutantResource())
            .build();

    @Test
    public void checkMutantPositive() {
        final Response response = makeRequest(new String[] {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        });
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final DNAResponse dnaResponse = response.readEntity(DNAResponse.class);
        assertTrue(dnaResponse.isMutant());
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

    }

    private Response makeRequest(String[] dna) {
        return resources.target("/mutant")
                .request()
                .post(Entity.entity(new DNARequestPayload(dna), MediaType.APPLICATION_JSON_TYPE));
    }
}