package org.distbc;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Created by mhelmich on 10/4/16.
 */
@Path("/distbc")
public interface DistBufferCacheResource {
    @GET
    @Path("/feedData/{index}")
    @Timed
    String feedData(@PathParam("index") String index);

    @GET
    @Path("/query")
    @Timed
    String query(@QueryParam("name") Optional<String> name);
}
