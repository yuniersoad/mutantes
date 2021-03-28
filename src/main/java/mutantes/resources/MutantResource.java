package mutantes.resources;

import mutantes.api.DNARequestPayload;
import mutantes.api.DNAResponse;
import mutantes.core.Detector;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/mutant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MutantResource {

    @POST
    public Response checkMutant(@NotNull @Valid DNARequestPayload dna){
        final boolean isMutant = Detector.isMutant(dna.getDna());
        final DNAResponse responsePayload = new DNAResponse(isMutant);
        return Response.status(isMutant ? Status.OK: Status.FORBIDDEN)
                .entity(responsePayload)
                .build();
    }
}
