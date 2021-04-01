package mutantes.resources;

import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import mutantes.api.StatsResponse;
import mutantes.core.Detector;
import mutantes.core.Subject;
import mutantes.db.SubjectRepository;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MutantResource {
    private final static Logger log = LoggerFactory.getLogger(MutantResource.class);
    public static String HUMAN_COUNT_CACHE_KEY = "HUMANS";
    public static String MUTANT_COUNT_CACHE_KEY = "MUTANTS";

    private final SubjectRepository repo;
    private final Jedis cache;

    public MutantResource(SubjectRepository subjectRepository, Jedis cache) {
        this.repo = subjectRepository;
        this.cache = cache;
    }

    @POST()
    @Path("/mutant")
    @ManagedAsync
    public void checkMutant(@Suspended AsyncResponse asyncResponse,
                            @NotNull @Valid DNARequestPayload dna) {
        repo.find(dna.getDna()).whenComplete((subjectRecordO, error) -> {
            // Just log DB errors so we can detect the issue, but keep processing the request since mutant detection is still possible
            if (error != null) log.error("Error fetching record: ", error);

            boolean isMutant = subjectRecordO
                    .map(Subject::getMutant)
                    .orElseGet(() -> {
                        Subject subject = new Subject(Arrays.asList(dna.getDna()), Detector.isMutant(dna.getDna()));
                        save(subject);
                        return subject.getMutant();
                    });

            final DNAResponse responsePayload = new DNAResponse(isMutant);
            asyncResponse.resume(Response.status(isMutant ? Status.OK: Status.FORBIDDEN)
                    .entity(responsePayload)
                    .build());

        });
    }

    @GET
    @Path("/stats")
    public Response getStats(){
        final Pipeline p = cache.pipelined();
        final redis.clients.jedis.Response<String> humanCount = p.get(HUMAN_COUNT_CACHE_KEY);
        final redis.clients.jedis.Response<String> mutantCount = p.get(MUTANT_COUNT_CACHE_KEY);
        p.sync();
        final StatsResponse statsResponse = new StatsResponse(mutantCount.get(), humanCount.get());
        return Response.ok().entity(statsResponse).build();
    }

    private void save(Subject subject) {
        repo.save(subject).thenAccept((v) -> {
            try { // Only inc stats if saving to the DB was successful to keep consistency
                cache.incr(subject.getMutant() ? MUTANT_COUNT_CACHE_KEY : HUMAN_COUNT_CACHE_KEY);
            } catch (JedisException e) {
                log.error("Error inc stats cache: ", e);
            }
        }).exceptionally((e) -> {
            log.error("Error saving record", e);
            return null;
        });
    }
}
