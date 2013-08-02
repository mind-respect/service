package org.triple_brain.service.vertex;

import com.sun.jersey.api.client.ClientResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.triple_brain.module.model.json.graph.GraphJSONFields;
import org.triple_brain.service.utils.GraphManipulationRestTest;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.triple_brain.module.model.json.graph.GraphJSONFields.VERTICES;

/*
* Copyright Mozilla Public License 1.1
*/
public class VertexSurroundGraphResouceTest extends GraphManipulationRestTest {

    @Test
    public void response_status_is_ok_for_getting_graph() {
        assertThat(
                getGraph().getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

    @Test
    public void can_get_graph() {
        JSONObject graph = getGraph().getEntity(JSONObject.class);
        JSONObject vertexAFromGraph = graph.optJSONObject(
                GraphJSONFields.VERTICES
        );
        assertThat(
                vertexAFromGraph.length(),
                is(3)
        );
    }

    @Test
    public void cant_get_graph_using_central_vertex_of_another_user() {
        NewCookie newCookie = authenticate(
                createAUser()
        ).getCookies().get(0);
        JSONArray vertices = graphUtils().makeGraphHave3SerialVerticesWithLongLabelsUsingCookie(
                newCookie
        );
        URI anotherUserVertexUri = vertexUtils().uriOfVertex(
                vertices.optJSONObject(0)
        );
        authenticate(defaultAuthenticatedUser);
        ClientResponse response = getGraphOfCentralVertexUriAtDepth(
                anotherUserVertexUri,
                5
        );
        assertThat(
                response.getStatus(),
                is(Response.Status.FORBIDDEN.getStatusCode())
        );
    }

    @Test
    public void can_get_sub_graph() throws Exception {
        JSONObject graph = getGraphOfCentralVertexUriAtDepth(
                vertexAUri(),
                2
        ).getEntity(JSONObject.class);
        assertThat(
                graph.getJSONObject(VERTICES).length(),
                is(3)
        );
        graph = getGraphOfCentralVertexUriAtDepth(
                vertexAUri(),
                1
        ).getEntity(JSONObject.class);
        assertThat(
                graph.getJSONObject(VERTICES).length(),
                is(2)
        );
    }

    @Test
    @Ignore("Getting graph of another user not implemented yet")
    public void cant_get_private_surround_vertices_of_public_vertex() {
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        authenticate(
                createAUser()
        );
        ClientResponse clientResponse = getGraphOfCentralVertexUriAtDepth(
                vertexAUri(),
                5
        );
        assertThat(
                clientResponse.getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
        JSONObject graph = clientResponse.getEntity(JSONObject.class);
        JSONObject vertices = graph.optJSONObject(
                GraphJSONFields.VERTICES
        );
        JSONObject edges = graph.optJSONObject(
                GraphJSONFields.EDGES
        );
        assertThat(
                vertices.length(),
                is(1)
        );
        assertThat(
                edges.length(),
                is(0)
        );
    }

    private ClientResponse getGraph() {
        return getGraphOfCentralVertexUriAtDepth(
                vertexAUri(),
                5
        );
    }

    private ClientResponse getGraphOfCentralVertexUriAtDepth(URI centralVertexUri, Integer depth) {
        return resource
                .path(centralVertexUri.getPath())
                .path("surround_graph")
                .path(depth.toString())
                .cookie(authCookie)
                .get(ClientResponse.class);
    }
}