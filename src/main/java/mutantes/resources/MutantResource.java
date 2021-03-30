package mutantes.resources;

import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import mutantes.core.Detector;
import mutantes.db.Subject;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

@Path("/mutant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MutantResource {
    private final static Logger log = LoggerFactory.getLogger(MutantResource.class);
    public static String HUMAN_COUNT_CACHE_KEY = "HUMANS";
    public static String MUTANT_COUNT_CACHE_KEY = "MUTANTS";

    private final DynamoDbAsyncTable<Subject> table;
    private final JedisCommands cache;

    public MutantResource(DynamoDbAsyncTable<Subject> table, JedisCommands cache) {
        this.table = table;
        this.cache = cache;
    }

    @POST
    @ManagedAsync
    public void checkMutant(@Suspended AsyncResponse asyncResponse,
                            @NotNull @Valid DNARequestPayload dna) {
        final Subject subject = new Subject(Arrays.asList(dna.getDna()));
        final CompletionStage<Subject> subjectF = table.getItem(subject);

        subjectF.whenComplete((subjectRecord, error) -> {
            // Just log DB errors so we can detect the issue, but keep processing the request since mutant detection is still possible
            if (error != null) log.error("Error fetching record: ", error);

            final boolean isMutant;
            if (subjectRecord != null) {
                isMutant = subjectRecord.getMutant();
            } else {
                isMutant = Detector.isMutant(dna.getDna());
                subject.setMutant(isMutant);
                table.putItem(subject).thenAccept((v) -> {
                    try { // Only inc stats if saving to the DB was successful to keep consistency
                        cache.incr(isMutant ? MUTANT_COUNT_CACHE_KEY: HUMAN_COUNT_CACHE_KEY);
                    } catch (JedisException e){
                        log.error("Error inc stats cache: ", e);
                    }
                }).exceptionally((e) -> {log.error("Error saving record", e); return null;});
            }

            final DNAResponse responsePayload = new DNAResponse(isMutant);
            asyncResponse.resume(Response.status(isMutant ? Status.OK: Status.FORBIDDEN)
                    .entity(responsePayload)
                    .build());

        });
    }
}
