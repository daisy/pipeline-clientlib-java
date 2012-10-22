package org.daisy.pipeline.client.cli;

import org.daisy.pipeline.client.Pipeline2WS;

/**
 * CLI frontend for the Pipeline2 Java Client (pipeline-clientlib-java).
 * 
 * @author jostein
 */
public class CLI {
	
	public class Config {
		public String endpoint = "http://localhost:8181/ws";
	}
	
    public static void main( String[] args ) {
    	Pipeline2WS.debug = true;
    	
    	Pipeline2WS.handleCallbacks(new CallbackHandler(), 8180);
    	
//    	Pipeline2WSResponse response = null;
//    	try {
//			response = Pipeline2WS.get(Config.endpoint, "/alive", null, null, null);
//			System.out.println(response.asText());
//			
//		} catch (Pipeline2WSException e) {
//			e.printStackTrace();
//			System.out.println("An error occured while attempting to communicate with the Pipeline 2 web service");
//		}
    	
    	try {
			Thread.sleep(60000L);
		} catch (InterruptedException e) {
			System.err.println("Could not sleep");
			e.printStackTrace(System.err);
		}
    }
}
