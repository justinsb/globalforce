package us.globalforce.guice;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.servlet.DefaultServlet;

import us.globalforce.resources.DemoREST;
import us.globalforce.resources.OAuthServlet;
import us.globalforce.resources.RootResource;
import us.globalforce.resources.TasksResource;
import us.globalforce.resources.WorkersResource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.inject.Scopes;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class GfGuiceServletModule extends JerseyServletModule {
    @Override
    protected void configureServlets() {
        // boolean USE_GZIP = true;
        // if (USE_GZIP) {
        // bind(GzipFilter.class).in(Scopes.SINGLETON);
        //
        // Map<String, String> params = new HashMap<String, String>();
        // params.put("mimeType",
        // "text/html,text/plain,text/xml,application/xhtml+xml,text/css,application/javascript,image/svg+xml,application/json");
        // filter("/*").through(GzipFilter.class, params);
        // }

        // install(new JpaPersistModule("mainJpaUnit")); // like we saw earlier.

        // filter("/*").through(PersistFilter.class);

        bind(RootResource.class);
        bind(WorkersResource.class);
        bind(TasksResource.class);

        serve("/DemoREST").with(DemoREST.class);
        serve("/oauth").with(OAuthServlet.class);
        serve("/oauth/*").with(OAuthServlet.class);

        // Configure Jackson for JSON output
        {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(Include.NON_NULL);

            // Use JAXB annotations
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
            objectMapper = objectMapper.setAnnotationIntrospector(introspector);

            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider(objectMapper);
            bind(JacksonJsonProvider.class).toInstance(jacksonJsonProvider);
        }

        {
            Map<String, String> params = new HashMap<String, String>();
            bind(DefaultServlet.class).in(Scopes.SINGLETON);

            params.put("dirAllowed", "false");
            params.put("gzip", "true");

            URL urlStatic = getClass().getResource("/webapp");
            params.put("resourceBase", urlStatic.toString());

            serve("/static/*").with(DefaultServlet.class, params);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, GZIPContentEncodingFilter.class.getName());
        params.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, GZIPContentEncodingFilter.class.getName());
        serve("/*").with(GuiceContainer.class, params);
    }
}