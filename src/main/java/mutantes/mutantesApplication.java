package mutantes;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import mutantes.resources.MutantResource;

public class mutantesApplication extends Application<mutantesConfiguration> {

    public static void main(final String[] args) throws Exception {
        new mutantesApplication().run(args);
    }

    @Override
    public String getName() {
        return "mutantes";
    }

    @Override
    public void initialize(final Bootstrap<mutantesConfiguration> bootstrap) {

    }

    @Override
    public void run(final mutantesConfiguration configuration,
                    final Environment environment) {
        environment.jersey().register(new MutantResource());
    }
}
