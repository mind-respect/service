/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources.vertex;

import com.sun.jersey.api.client.ClientResponse;
import guru.bubl.module.model.center_graph_element.CenterGraphElementPojo;
import guru.bubl.module.model.graph.graph_element.GraphElementOperator;
import guru.bubl.module.model.graph.subgraph.SubGraphPojo;
import guru.bubl.module.model.graph.vertex.Vertex;
import guru.bubl.module.model.search.GraphElementSearchResult;
import guru.bubl.service.SessionHandler;
import guru.bubl.service.utils.GraphManipulationRestTestUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VertexResourceTest extends GraphManipulationRestTestUtils {

    @Test
    public void can_remove_a_vertex() {
        assertTrue(graphElementWithIdExistsInCurrentGraph(
                vertexBUri()
        ));
        vertexUtils().removeVertexB();
        assertFalse(graphElementWithIdExistsInCurrentGraph(
                vertexBUri()
        ));
    }

    @Test
    public void removing_vertex_returns_correct_response_status() {
        ClientResponse response = vertexUtils().removeVertexB();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void can_update_label() {
        String vertexALabel = vertexA().label();
        assertThat(vertexALabel, is(not("new vertex label")));
        vertexUtils().updateVertexLabelUsingRest(
                vertexAUri(),
                "new vertex label"
        );
        vertexALabel = vertexA().label();
        assertThat(vertexALabel, is("new vertex label"));
    }

    @Test
    public void label_can_have_special_characters() {
        String vertexALabel = vertexA().label();
        assertThat(vertexALabel, is(not("a(test*)")));
        vertexUtils().updateVertexLabelUsingRest(
                vertexAUri(),
                "a(test*)"
        );
        vertexALabel = vertexA().label();
        assertThat(vertexALabel, is("a(test*)"));
    }

    @Test
    public void can_update_note() {
        String vertexANote = vertexA().comment();
        assertThat(vertexANote, is(not("some note")));
        vertexUtils().updateVertexANote("some note");
        vertexANote = vertexA().comment();
        assertThat(vertexANote, is("some note"));
    }

    @Test
    public void updating_label_returns_correct_status() {
        ClientResponse response = vertexUtils().updateVertexLabelUsingRest(
                vertexAUri(),
                "new vertex label"
        );
        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    @Test
    public void when_deleting_a_vertex_its_relations_are_also_removed_from_search() {
        searchUtils().indexAll();
        List<GraphElementSearchResult> relations = searchUtils().searchForRelations(
                "edge",
                defaultAuthenticatedUserAsJson
        );
        assertThat(
                relations.size(),
                is(4)
        );
        vertexUtils().removeVertexB();
        relations = searchUtils().searchForRelations(
                "edge",
                defaultAuthenticatedUserAsJson
        );
        assertThat(
                relations.size(),
                is(2)
        );
    }

    @Test
    public void remove_returns_ok_status() {
        ClientResponse response = vertexUtils().removeVertexB();
        assertThat(
                response.getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

//    @Test
//    @Test("searching for public vertices is suspended")
//    public void making_vertex_public_re_indexes_it() {
//        searchUtils().indexAll();
//        JSONObject anotherUser = createAUser();
//        authenticate(
//                anotherUser
//        );
//        List<GraphElementSearchResult> results = searchUtils().autoCompletionResultsForPublicAndUserVertices(
//                vertexA().label(),
//                anotherUser
//        );
//        assertThat(
//                results.size(), is(0)
//        );
//        authenticate(defaultAuthenticatedUser);
//        vertexUtils().makePublicVertexWithUri(
//                vertexAUri()
//        );
//        authenticate(anotherUser);
//        results = searchUtils().autoCompletionResultsForPublicAndUserVertices(
//                vertexA().label(),
//                anotherUser
//        );
//        assertThat(
//                results.size(),
//                is(1)
//        );
//    }

//    @Test
//    @Test("searching for public vertices is suspended")
//    public void making_vertex_private_re_indexes_it() {
//        vertexUtils().makePublicVertexWithUri(
//                vertexAUri()
//        );
//        searchUtils().indexAll();
//        JSONObject anotherUser = createAUser();
//        authenticate(anotherUser);
//        JSONArray results = searchUtils().clientResponseOfAutoCompletionForPublicAndUserOwnedVertices(
//                vertexA().label(),
//                anotherUser
//        ).getEntity(JSONArray.class);
//        Assert.assertThat(results.length(), Is.is(greaterThan(0)));
//        authenticate(defaultAuthenticatedUser);
//        vertexUtils().makePrivateVertexWithUri(
//                vertexAUri()
//        );
//        authenticate(anotherUser);
//        results = searchUtils().clientResponseOfAutoCompletionForPublicAndUserOwnedVertices(
//                vertexA().label(),
//                anotherUser
//        ).getEntity(JSONArray.class);
//        Assert.assertThat(results.length(), Is.is(0));
//    }

    @Test
    public void number_of_connected_vertices_are_included() {
        assertThat(
                vertexB().getNbNeighbors().getPrivate(),
                is(2)
        );
    }

    @Test
    public void creating_a_single_vertex_increment_its_number_of_visits() {
        List<CenterGraphElementPojo> centerElements = graphUtils().getCenterGraphElements();
        Integer numberOfVisitedElements = centerElements.size();
        vertexUtils().createSingleVertex();
        centerElements = graphUtils().getCenterGraphElements();
        Assert.assertThat(
                centerElements.size(),
                Is.is(numberOfVisitedElements + 1)
        );
    }

    @Test
    public void creating_a_single_vertex_sets_last_center_date() {
        vertexUtils().createSingleVertex();
        List<CenterGraphElementPojo> centerElements = graphUtils().getCenterGraphElements();
        Integer numberOfLastCenterDateNull = 0;
        for (CenterGraphElementPojo centerGraphElementPojo : centerElements) {
            if (null == centerGraphElementPojo.getLastCenterDate()) {
                numberOfLastCenterDateNull++;
            }
        }
        assertThat(
                numberOfLastCenterDateNull,
                is(0)
        );
    }

    @Test
    public void can_set_children_indexes() throws Exception {
        JSONObject childrenIndexes = new JSONObject().put(
                vertexAUri().toString(),
                new JSONObject().put(
                        "index",
                        0
                )
        ).put(
                vertexCUri().toString(),
                new JSONObject().put(
                        "index",
                        1
                )
        );
        SubGraphPojo subGraph = graphUtils().graphWithCenterVertexUri(vertexBUri());
        assertFalse(
                subGraph.vertexWithIdentifier(vertexBUri()).getChildrenIndex().equals(
                        childrenIndexes.toString()
                )
        );
        ClientResponse response = setChildrenIndexes(vertexB(), childrenIndexes);
        assertThat(
                response.getStatus(),
                is(Response.Status.NO_CONTENT.getStatusCode())
        );
        subGraph = graphUtils().graphWithCenterVertexUri(vertexBUri());
        assertTrue(
                subGraph.vertexWithIdentifier(vertexBUri()).getChildrenIndex().equals(
                        childrenIndexes.toString()
                )
        );
    }

    @Test
    public void can_set_colors() throws Exception {
        JSONObject colors =
                new JSONObject().put(
                        GraphElementOperator.colorProps.background.toString(),
                        "blue"
                );

        SubGraphPojo subGraph = graphUtils().graphWithCenterVertexUri(vertexBUri());
        assertFalse(
                subGraph.vertexWithIdentifier(vertexBUri()).getColors().equals(
                        colors.toString()
                )
        );
        ClientResponse response = setColors(vertexB(), colors);
        assertThat(
                response.getStatus(),
                is(Response.Status.NO_CONTENT.getStatusCode())
        );
        subGraph = graphUtils().graphWithCenterVertexUri(vertexBUri());
        assertTrue(
                subGraph.vertexWithIdentifier(vertexBUri()).getColors().equals(
                        colors.toString()
                )
        );
    }

    @Test
    public void make_pattern_returns_no_content_status() {
        assertThat(
                vertexUtils().makePattern(vertexAUri()).getStatus(),
                is(Response.Status.NO_CONTENT.getStatusCode())
        );
    }

    @Test
    public void bad_request_status_when_trying_to_make_pattern_a_vertex_under_a_pattern() {
        vertexUtils().makePattern(vertexAUri());
        assertThat(
                vertexUtils().makePattern(vertexBUri()).getStatus(),
                is(Response.Status.BAD_REQUEST.getStatusCode())
        );
    }

//    @Test
//    @Test(
//            "Using measures on the client side to avoid fast addition of multiple childs. " +
//                    "Also I consider it not dramatic that the number of connected edges is not totally accurate."
//    )
//    public void number_of_connected_edges_is_ok_after_adding_vertices_concurrently() {
//        assertThat(
//                vertexA().getNumberOfConnectedEdges(),
//                is(1)
//        );
//        Integer numberOfVerticesToAdd = 5;
//        CountDownLatch latch = new CountDownLatch(numberOfVerticesToAdd);
//        for (int i = 0; i < numberOfVerticesToAdd; i++) {
//            new Thread(new AddChildToVertexARunner(latch)).start();
////            Thread.sleep(50);
//        }
//        try {
//            latch.await();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        assertThat(
//                vertexA().getNumberOfConnectedEdges(),
//                is(6)
//        );
//
//    }

    private class AddChildToVertexARunner implements Runnable {
        CountDownLatch latch = null;

        public AddChildToVertexARunner(CountDownLatch latch) {
            this.latch = latch;
        }

        public void run() {
            resource.path(
                    vertexAUri().getPath()
            ).cookie(
                    authCookie
            ).post();
            latch.countDown();
        }
    }

    private ClientResponse setChildrenIndexes(Vertex vertex, JSONObject childrenIndexes) {
        return resource
                .path(
                        vertex.uri().getPath()
                )
                .path("childrenIndex")
                .cookie(authCookie)
                .header(SessionHandler.X_XSRF_TOKEN, currentXsrfToken)
                .post(ClientResponse.class, childrenIndexes);
    }

    private ClientResponse setColors(Vertex vertex, JSONObject colors) {
        return resource
                .path(
                        vertex.uri().getPath()
                )
                .path("colors")
                .cookie(authCookie)
                .header(SessionHandler.X_XSRF_TOKEN, currentXsrfToken)
                .post(ClientResponse.class, colors);
    }
}
