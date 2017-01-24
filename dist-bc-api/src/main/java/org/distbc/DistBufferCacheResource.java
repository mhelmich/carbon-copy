package org.distbc;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/distbc")
public interface DistBufferCacheResource {
    @GET
    @Path("/feedData/{index}")
    @Timed
    String feedData(@PathParam("index") String index);

    @GET
    @Path("/query")
    @Timed
    String query(@QueryParam("name") String name);
}
