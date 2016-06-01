package org.tmf.dsmapi;




import javax.ws.rs.ApplicationPath;
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
        register(org.tmf.dsmapi.usage.UsageResource.class);
        register(org.tmf.dsmapi.usage.UsageAdminResource.class);
        register(org.tmf.dsmapi.usageSpecification.UsageSpecificationResource.class);
        register(org.tmf.dsmapi.usageSpecification.UsageSpecificationAdminResource.class);
    }

   
    
}

