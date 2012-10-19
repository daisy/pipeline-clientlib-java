package org.daisy.pipeline.client.cli;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;

/**
 * CLI frontend for the Pipeline2 Java Client (pipeline-clientlib-java).
 * 
 * @author jostein
 */
public class CLI {
    public static void main( String[] args ) {
    	Pipeline2WS.debug = true;
    	Pipeline2WSResponse response = null;
    	try {
			response = Pipeline2WS.get("http://localhost:8181/ws", "/alive", null, null, null);
			System.out.println(response.asText());
			
		} catch (Pipeline2WSException e) {
			e.printStackTrace();
			System.out.println("An error occured while attempting to communicate with the Pipeline 2 web service");
		}
    }
}
