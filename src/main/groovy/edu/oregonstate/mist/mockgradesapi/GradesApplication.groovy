package edu.oregonstate.mist.mockgradesapi

import edu.oregonstate.mist.api.BuildInfoManager
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.InfoResource
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.BasicAuthenticator
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.auth.AuthFactory
import io.dropwizard.auth.basic.BasicAuthFactory

/**
 * Main application class.
 */
class GradesApplication extends Application<Configuration> {
    /**
     * Initializes application bootstrap.
     *
     * @param bootstrap
     */
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {}

    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    public void run(Configuration configuration, Environment environment) {
        Resource.loadProperties()

        BuildInfoManager buildInfoManager = new BuildInfoManager()
        environment.lifecycle().manage(buildInfoManager)

        environment.jersey().register(new InfoResource(buildInfoManager.getInfo()))
        environment.jersey().register(
                AuthFactory.binder(
                        new BasicAuthFactory<AuthenticatedUser>(
                                new BasicAuthenticator(configuration.getCredentialsList()),
                                'GradesApplication',
                                AuthenticatedUser.class)))

        environment.jersey().register(new GradesResource(configuration.api.endpointUri, configuration.api.gradesJsonPath))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    public static void main(String[] arguments) throws Exception {
        new GradesApplication().run(arguments)
    }
}
