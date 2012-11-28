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
	
	public static String endpoint = "http://localhost:8181/ws";
	
    public static void main( String[] args ) {
    	// TODO: load config from config.yml ?
    	
    	Pipeline2WS.debug = true;
    	
    	Pipeline2WS.handleCallbacks(new CallbackHandler(), 8180);
    	
    	Pipeline2WSResponse response = null;
    	try {
			response = Pipeline2WS.get(endpoint, "/alive", null, null, null);
			System.out.println(response.asText());
			
		} catch (Pipeline2WSException e) {
			System.out.println("Unable to communicate with the Pipeline 2 framework");
			System.exit(-1);
		}
    	
    	// TODO: parse arguments
    	for (String arg : args) {
    		System.out.println("Argument: "+arg);
    	}
    	
    	boolean finished = false;
    	while (!finished) {
    		
	    	try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				System.err.println("Could not sleep");
				e.printStackTrace(System.err);
			}
	    	
	    	finished = true;
    	}
    	
    	try {
			Pipeline2WS.stopCallbacks();
		} catch (Exception e) {
			System.err.println("Unable to stop callbacks web service");
			e.printStackTrace();
			System.exit(-1);
		}
    }
}
