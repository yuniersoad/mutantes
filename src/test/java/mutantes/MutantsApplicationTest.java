package mutantes;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MutantsApplicationTest {

    // Notice that the external DBs & cache won't be available or mocked starting the application this way
    // Still the endpoint is responsive (confirm if this is the desired behavior)
    @ClassRule
    public static final DropwizardAppRule<MutantsConfiguration> RULE =
            new DropwizardAppRule<>(MutantsApplication.class, ResourceHelpers.resourceFilePath("./config.tests.yml"));

    @Test
    public void testMutant() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        DNARequestPayload payload = new DNARequestPayload(dna);

        Response response = RULE.client().target(
                String.format("http://127.0.0.1:%d/mutant", RULE.getLocalPort()))
                .request()
                .post(Entity.json(payload));

        assertEquals(200, response.getStatus());
        final DNAResponse dnaResponse = response.readEntity(DNAResponse.class);
        assertTrue(dnaResponse.isMutant());
    }

}