package com.tinkerpop.rexster;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.rexster.traversals.ElementJSONObject;
import org.json.simple.JSONArray;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeResource extends BaseResource {


    @Get
    public Representation getResource() {
        Map<String, String> queryParameters = createQueryMap(this.getRequest().getResourceRef().getQueryAsForm());
        this.buildRequestObject(queryParameters);

        String id = (String) getRequest().getAttributes().get(Tokens.ID);
        String graphName = this.getRequest().getResourceRef().getSegments().get(0);
        if (null == id)
            getAllEdges(graphName);
        else
            getSingleEdge(graphName, id);


        this.resultObject.put(Tokens.QUERY_TIME, sh.stopWatch());
        return RexsterResponse.getStringRepresentation(this.getRequest(), this.resultObject);
    }

    @Post
    public Representation postResource() {

        this.buildRequestObject(createQueryMap(this.getRequest().getResourceRef().getQueryAsForm()));

        String graphName = this.getRequest().getResourceRef().getSegments().get(0);
        final Graph graph = this.getRexsterApplication().getGraph(graphName);
        final String id = (String) getRequest().getAttributes().get(Tokens.ID);
        String inV = null;
        Object temp = this.requestObject.get(Tokens._IN_V);
        if (null != temp)
            inV = temp.toString();
        String outV = null;
        temp = this.requestObject.get(Tokens._OUT_V);
        if (null != temp)
            outV = temp.toString();
        String label = null;
        temp = this.requestObject.get(Tokens._LABEL);
        if (null != temp)
            label = temp.toString();

        Edge edge = graph.getEdge(id);
        if (null == edge && null != outV && null != inV && null != label) {
            final Vertex out = graph.getVertex(outV);
            final Vertex in = graph.getVertex(inV);
            if (null != out && null != in)
                edge = graph.addEdge(id, out, in, label);
        }
        if (null != edge) {
            for (final String key : (Set<String>) this.requestObject.keySet()) {
                if (!key.startsWith(Tokens.UNDERSCORE))
                    edge.setProperty(key, this.requestObject.get(key));
            }
            this.resultObject.put(Tokens.RESULTS, new ElementJSONObject(edge, this.getReturnKeys()));
        }

        this.resultObject.put(Tokens.QUERY_TIME, sh.stopWatch());

        return RexsterResponse.getStringRepresentation(this.getRequest(), this.resultObject);
    }

    @Delete
    public Representation deleteResource() {
        // TODO: delete individual properties
        final String id = (String) getRequest().getAttributes().get(Tokens.ID);

        String graphName = this.getRequest().getResourceRef().getSegments().get(0);
        final Graph graph = this.getRexsterApplication().getGraph(graphName);
        final Edge edge = graph.getEdge(id);
        if (null != edge)
            graph.removeEdge(edge);

        this.resultObject.put(Tokens.QUERY_TIME, sh.stopWatch());
        return RexsterResponse.getStringRepresentation(this.getRequest(), this.resultObject);

    }

    public void getAllEdges(String graphName) {
        Long start = this.getStartOffset();
        if (null == start)
            start = 0l;
        Long end = this.getEndOffset();
        if (null == end)
            end = Long.MAX_VALUE;

        long counter = 0l;
        JSONArray edgeArray = new JSONArray();
        for (Edge edge : this.getRexsterApplication().getGraph(graphName).getEdges()) {
            if (counter >= start && counter < end) {
                edgeArray.add(new ElementJSONObject(edge, this.getReturnKeys()));
            }
            counter++;
        }
        this.resultObject.put(Tokens.RESULTS, edgeArray);
        this.resultObject.put(Tokens.TOTAL_SIZE, counter);
        this.resultObject.put(Tokens.QUERY_TIME, sh.stopWatch());

    }

    public void getSingleEdge(String graphName, final Object id) {
        final Edge edge = this.getRexsterApplication().getGraph(graphName).getEdge(id);
        if (null != edge) {
            this.resultObject.put(Tokens.RESULTS, new ElementJSONObject(edge, this.getReturnKeys()));
        } else {
            this.resultObject.put(Tokens.RESULTS, null);
        }
    }
}
