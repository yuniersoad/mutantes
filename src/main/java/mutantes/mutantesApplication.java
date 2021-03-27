package mutantes;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
        // TODO: application initialization
    }

    @Override
    public void run(final mutantesConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
