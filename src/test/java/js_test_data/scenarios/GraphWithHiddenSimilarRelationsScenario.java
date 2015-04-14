/*
 * Copyright Vincent Blouin under the Mozilla Public License 1.1
 */

package js_test_data.scenarios;

import js_test_data.JsTestScenario;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.triple_brain.module.model.User;
import org.triple_brain.module.model.graph.GraphFactory;
import org.triple_brain.module.model.graph.ModelTestScenarios;
import org.triple_brain.module.model.graph.SubGraphPojo;
import org.triple_brain.module.model.graph.UserGraph;
import org.triple_brain.module.model.graph.edge.EdgeOperator;
import org.triple_brain.module.model.graph.vertex.VertexFactory;
import org.triple_brain.module.model.graph.vertex.VertexOperator;
import org.triple_brain.module.model.json.graph.SubGraphJson;

import javax.inject.Inject;

public class GraphWithHiddenSimilarRelationsScenario implements JsTestScenario {

    /*
     * b1-r1->b2
     * b2 has hidden relations
     * b2-T-shirt->shirt1
     * b2-T-shirt->shirt2
     * relations T-shirt are identified to Freebase T-shirt.
     */

    @Inject
    protected GraphFactory graphFactory;

    @Inject
    protected VertexFactory vertexFactory;

    @Inject
    ModelTestScenarios modelTestScenarios;

    User user = User.withEmailAndUsername("a", "b");

    private VertexOperator
            b1,
            b2,
            shirt1,
            shirt2;
    @Override
    public JSONObject build() {
        UserGraph userGraph = graphFactory.createForUser(user);
        createVertices();
        createRelations();
        SubGraphPojo b1Graph = userGraph.graphWithDepthAndCenterVertexId(
                1,
                b1.uri()
        );
        SubGraphPojo b2Graph = userGraph.graphWithDepthAndCenterVertexId(
                1,
                b2.uri()
        );
        try {
            return new JSONObject().put(
                    "b1Graph",
                    SubGraphJson.toJson(b1Graph)
            ).put(
                    "b2Graph",
                    SubGraphJson.toJson(b2Graph)
            );
        }catch(JSONException e){
            throw new RuntimeException(e);
        }
    }

    private void createVertices(){
        b1 = vertexFactory.createForOwnerUsername(
                user.username()
        );
        b1.label("b1");
        b2 = vertexFactory.createForOwnerUsername(
                user.username()
        );
        b2.label("b2");
        shirt1 = vertexFactory.createForOwnerUsername(
                user.username()
        );
        shirt1.label("shirt1");
        shirt2 = vertexFactory.createForOwnerUsername(
                user.username()
        );
        shirt2.label("shirt2");
    }

    private void createRelations(){
        b1.addRelationToVertex(b2).label("r1");
        EdgeOperator shirt1Relation = b2.addRelationToVertex(shirt1);
        shirt1Relation.label("shirt1");
        shirt1Relation.addSameAs(modelTestScenarios.tShirt());
        EdgeOperator shirt2Relation = b2.addRelationToVertex(shirt2);
        shirt2Relation.label("shirt2");
        shirt2Relation.addSameAs(modelTestScenarios.tShirt());
    }
}