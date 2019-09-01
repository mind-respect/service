/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources;

import com.sun.jersey.api.client.ClientResponse;
import guru.bubl.module.model.json.UserJson;
import guru.bubl.module.model.search.GraphElementSearchResult;
import guru.bubl.service.utils.GraphManipulationRestTestUtils;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;

import static guru.bubl.module.model.json.UserJson.USER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore("fork feature suspended")
public class ForkResourceTest extends GraphManipulationRestTestUtils {

    @Test
    public void fork_returns_ok_status() {
        ClientResponse response = forkVertexABCGraph();
        assertThat(
                response.getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

    @Test
    public void returns_forbidden_if_forking_for_another_user() {
        JSONObject graphAsJson = graphUtils().getNonOwnedGraphOfCentralVertex(
                vertexA()
        ).getEntity(JSONObject.class);
        JSONObject otherUser = userUtils().validForCreation();
        createUser(otherUser);
        authenticate(defaultAuthenticatedUserAsJson);
        ClientResponse response = resource
                .path(getUsersBaseUri(otherUser.optString(USER_NAME, "")))
                .path("fork")
                .cookie(authCookie)
                .post(ClientResponse.class, graphAsJson);
        assertThat(
                response.getStatus(),
                is(Response.Status.FORBIDDEN.getStatusCode())
        );
    }

    @Test
    public void can_fork() {
        List<GraphElementSearchResult> relatedResources = searchUtils().autoCompletionResultsForPublicAndUserVertices(
                "vertex B",
                UserJson.toJson(currentAuthenticatedUser)
        );
        assertThat(
                relatedResources.size(),
                is(1)
        );
        vertexUtils().makePublicVertexWithUri(vertexBUri());
        forkVertexABCGraph();
        relatedResources = searchUtils().autoCompletionResultsForPublicAndUserVertices(
                "vertex B",
                UserJson.toJson(currentAuthenticatedUser)
        );
        assertThat(
                relatedResources.size(),
                is(3)
        );
    }

    private ClientResponse forkVertexABCGraph() {
        JSONObject graphAsJson = graphUtils().getNonOwnedGraphOfCentralVertex(
                vertexB()
        ).getEntity(JSONObject.class);
        JSONObject otherUser = userUtils().validForCreation();
        createUser(otherUser);
        authenticate(otherUser);
        return resource
                .path(getUsersBaseUri(otherUser.optString(USER_NAME, "")))
                .path("fork")
                .cookie(authCookie)
                .post(ClientResponse.class, graphAsJson);
    }
}

