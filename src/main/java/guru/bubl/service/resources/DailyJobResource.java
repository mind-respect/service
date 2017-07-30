/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources;

import guru.bubl.module.model.admin.WholeGraphAdminDailyJob;
import guru.bubl.module.model.graph.GraphTransactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/daily-job")
@Singleton
public class DailyJobResource {

    @Inject
    WholeGraphAdminDailyJob wholeGraphAdminDailyJob;

    @GET
    @GraphTransactional
    public Response doDailyJob(){
        System.out.println("daily job");
//        wholeGraphAdminDailyJob.execute();
        return Response.ok().build();
    }
}