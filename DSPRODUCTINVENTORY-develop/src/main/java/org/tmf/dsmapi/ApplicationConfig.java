package org.tmf.dsmapi;

import org.glassfish.jersey.server.ResourceConfig;

@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        packages ("org.codehaus.jackson.jaxrs");
        register(org.tmf.dsmapi.commons.jaxrs.BadUsageExceptionMapper.class);
        register(org.tmf.dsmapi.commons.jaxrs.JacksonConfigurator.class);
        register(org.tmf.dsmapi.commons.jaxrs.JsonMappingExceptionMapper.class);
        register(org.tmf.dsmapi.commons.jaxrs.UnknowResourceExceptionMapper.class);
        register(org.tmf.dsmapi.hub.HubResource.class);
        register(org.tmf.dsmapi.product.ProductAdminResource.class);
        register(org.tmf.dsmapi.product.ProductResource.class);
    }
}
