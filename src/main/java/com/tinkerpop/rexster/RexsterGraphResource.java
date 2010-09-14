package com.tinkerpop.rexster;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.rexster.traversals.Traversal;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraphResource extends ServerResource {

    @Get
    public Representation evaluate() {
        StatisticsHelper sh = new StatisticsHelper();
        sh.stopWatch();
        JSONObject resultObject = new JSONObject();
        resultObject.put("name", "Rexster: A RESTful Graph Shell");

        String graphName = this.getRequest().getResourceRef().getSegments().get(0);
        Graph graph = this.getRexsterApplication().getGraph(graphName);
        resultObject.put("graph", graph.toString());

        JSONArray queriesArray = new JSONArray();
        for (Map.Entry<String, Class<? extends Traversal>> traversal : this.getRexsterApplication().getLoadedTraversalServices(graphName).entrySet()) {
            queriesArray.add(traversal.getKey());
        }
        resultObject.put("traversals", queriesArray);

        resultObject.put("query_time", sh.stopWatch());
        resultObject.put("up_time", this.getTimeAlive());
        resultObject.put("version", RexsterApplication.getVersion());
        return RexsterResponse.getStringRepresentation(this.getRequest(), resultObject);
    }

    private String getTimeAlive() {
        long timeMillis = System.currentTimeMillis() - this.getRexsterApplication().getStartTime();
        long timeSeconds = timeMillis / 1000;
        long timeMinutes = timeSeconds / 60;
        long timeHours = timeMinutes / 60;
        long timeDays = timeHours / 24;

        String seconds = Integer.toString((int) (timeSeconds % 60));
        String minutes = Integer.toString((int) (timeMinutes % 60));
        String hours = Integer.toString((int) timeHours % 24);
        String days = Integer.toString((int) timeDays);

        for (int i = 0; i < 2; i++) {
            if (seconds.length() < 2) {
                seconds = "0" + seconds;
            }
            if (minutes.length() < 2) {
                minutes = "0" + minutes;
            }
            if (hours.length() < 2) {
                hours = "0" + hours;
            }
        }
        return days + "[d]:" + hours + "[h]:" + minutes + "[m]:" + seconds + "[s]";
    }

    protected RexsterApplication getRexsterApplication() {
        return (RexsterApplication) this.getApplication();
    }
}
