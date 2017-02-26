package org.distbc;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/distbc")
public interface DistBufferCacheResource {
    @GET
    @Path("/feedData/{index}")
    @Timed
    String feedData(@PathParam("index") String index);

    @POST
    @Path("/query")
    @Timed
    String query(@QueryParam("query") String query);
}
