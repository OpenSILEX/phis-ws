/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opensilex.service.itests;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import opensilex.service.authentication.Session;
import opensilex.service.injection.SessionFactory;
import opensilex.service.injection.SessionInject;
import opensilex.service.injection.SessionInjectResolver;
import opensilex.service.json.CustomJsonWriterReader;
import opensilex.service.resource.ResourceService;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 *
 * @author training
 */
public class InternalProviderIntegrationTestHelper extends JerseyTest {

    @Override
    public Application configure() {

//        enable(TestProperties.LOG_TRAFFIC);
//        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resConf = new ResourceConfig(ResourceService.class);

        resConf.register(MultiPartFeature.class);
        resConf.register(JacksonFeature.class);
        resConf.register(CustomJsonWriterReader.class);
        resConf.register(SessionFactory.class);

        resConf.packages("io.swagger.jaxrs.listing;"
                + "opensilex.service.resource;"
                + "opensilex.service.json;"
                + "opensilex.service.resource.request.filter"
        );

        return resConf;

    }

    @Override
    public TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        ResourceConfig config = (ResourceConfig) configure();
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(SessionFactory.class).to(Session.class);
                bind(SessionInjectResolver.class)
                        .to(new TypeLiteral<InjectionResolver<SessionInject>>() {
                        })
                        .in(Singleton.class);
            }
        });
        return ServletDeploymentContext.forServlet(
                new ServletContainer(config)).build();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        super.configureClient(config); //To change body of generated methods, choose Tools | Templates.
        config.register(CustomJsonWriterReader.class);
        config.register(MultiPartFeature.class);

    }

    protected void preTestCaseTrace(String testcaseName) {
        System.out.println("\n"
                + "|||⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻⎻"
                + ">>>>>>>>>>>>>>>>>>>>>>>>>> \nSTART TEST: " + testcaseName + "\n");
    }

    protected void postTestCaseTrace(String testcaseName) {
        System.out.println("\n" + "END TEST: " + testcaseName + "\n"
                + "______________________________________________________"
                + "_______________________|||"
                + "\n");
    }

}
