/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.service.resources.center;

import com.sun.jersey.api.client.ClientResponse;
import guru.bubl.module.model.center_graph_element.CenterGraphElementPojo;
import guru.bubl.service.SessionHandler;
import guru.bubl.service.resources.pattern.PatternConsumerResourceTest;
import guru.bubl.service.utils.GraphManipulationRestTestUtils;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;

import static guru.bubl.service.utils.GraphRestTestUtils.getCenterGraphElementsFromClientResponse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class PublicCenterGraphElementsResourceTest extends GraphManipulationRestTestUtils {

    @Test
    public void getting_public_center_graph_elements_returns_ok_status() {
        assertThat(
                graphUtils().getPublicCenterGraphElementsResponse().getStatus(),
                is(
                        Response.Status.OK.getStatusCode()
                )
        );
    }

    @Test
    public void returns_only_public_centers() {
        graphUtils().graphWithCenterVertexUri(vertexA().uri());
        List<CenterGraphElementPojo> centerElements = graphUtils().getCenterGraphElements();
        assertFalse(
                centerElements.isEmpty()
        );
        List<CenterGraphElementPojo> centers = graphUtils().getCenterGraphElementsFromClientResponse(
                graphUtils().getPublicCenterGraphElementsResponse()
        );
        assertThat(
                centers.size(),
                is(0)
        );
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        centers = graphUtils().getCenterGraphElementsFromClientResponse(
                graphUtils().getPublicCenterGraphElementsResponse()
        );
        assertThat(
                centers.size(),
                is(1)
        );
    }


    @Test
    public void can_get_public_centers_of_another_user() {
        graphUtils().graphWithCenterVertexUri(vertexA().uri());
        vertexUtils().makePublicVertexWithUri(
                vertexAUri()
        );
        createAUser();
        ClientResponse response = graphUtils().getPublicCenterGraphElementsResponseForUser(
                defaultAuthenticatedUser
        );
        assertThat(
                response.getStatus(),
                is(
                        Response.Status.OK.getStatusCode()
                )
        );
        List<CenterGraphElementPojo> centers = graphUtils().getCenterGraphElementsFromClientResponse(
                response
        );
        assertThat(
                centers.size(),
                is(1)
        );
    }

    @Test
    public void getting_list_of_patterns_returns_ok_status() {
        ClientResponse response = getPatternsListResponse();
        assertThat(
                response.getStatus(),
                is(Response.Status.OK.getStatusCode())
        );
    }

    @Test
    public void can_get_list_of_patterns() {
        List<CenterGraphElementPojo> patterns = getPatternsList();
        assertThat(
                patterns.size(),
                is(0)
        );
        ClientResponse response = PatternConsumerResourceTest.makePattern(vertexAUri(), authCookie, currentXsrfToken, false);
        assertThat(
                response.getStatus(),
                is(Response.Status.NO_CONTENT.getStatusCode())
        );
        patterns = getPatternsList();
        assertThat(
                patterns.size(),
                is(1)
        );
    }

    @Test
    public void can_get_list_of_patterns_when_non_centers() {
        PatternConsumerResourceTest.makePattern(vertexCUri(), authCookie, currentXsrfToken, true);
        List<CenterGraphElementPojo> patterns = getPatternsList();
        assertThat(
                patterns.size(),
                is(1)
        );
    }

    private ClientResponse getPatternsListResponse() {
        return resource
                .path("service")
                .path("center-elements")
                .path("public")
                .path("pattern")
                .cookie(authCookie)
                .header(SessionHandler.X_XSRF_TOKEN, currentXsrfToken)
                .get(ClientResponse.class);
    }

    private List<CenterGraphElementPojo> getPatternsList() {
        return getCenterGraphElementsFromClientResponse(getPatternsListResponse());
    }
}
