package org.daisy.pipeline.client;

/**
 * Methods for communicating with the "/alive" resource in a Pipeline 2 Web Service.
 * 
 * @see http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI#Alive
 * 
 * @author jostein
 */
public class Alive {

	/**
	 * Get information about the framework
	 * 
	 * HTTP 200 OK: Response body contains XML data
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse get(String endpoint) throws Pipeline2WSException {
		return Pipeline2WS.get(endpoint, "/alive", null, null, null);
	}
	
	
	// -- convenience functions:
	
	/**
	 * Convenience wrapper around get(endpoint). Returns whether or not the WS at `endpoint` is alive. 
	 */
	public static Boolean isAlive(String endpoint) {
		try {
			org.daisy.pipeline.client.models.Alive alive = new org.daisy.pipeline.client.models.Alive(get(endpoint));
			if (alive.error)
				return false;
			
		} catch (Pipeline2WSException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Convenience wrapper around get(endpoint). Returns whether or not the WS at `endpoint` uses authentication. 
	 */
	public static Boolean usesAuthentication(String endpoint) {
		try {
			org.daisy.pipeline.client.models.Alive alive = new org.daisy.pipeline.client.models.Alive(get(endpoint));
			return alive.authentication;
			
		} catch (Pipeline2WSException e) {
			return null;
		}
	}
	
	/**
	 * Convenience wrapper around get(endpoint). Returns whether or not the WS at `endpoint` exposes file URIs referencing the local filesystem (previously known as "local mode"). 
	 */
	public static Boolean allowsAccessToLocalFilesystem(String endpoint) {
		try {
			org.daisy.pipeline.client.models.Alive alive = new org.daisy.pipeline.client.models.Alive(get(endpoint));
			return alive.localfs;
			
		} catch (Pipeline2WSException e) {
			return null;
		}
	}
	
	/**
	 * Convenience wrapper around get(endpoint). Returns the version of the WS at `endpoint`. 
	 */
	public static String getVersion(String endpoint) {
		try {
			org.daisy.pipeline.client.models.Alive alive = new org.daisy.pipeline.client.models.Alive(get(endpoint));
			return alive.version;
			
		} catch (Pipeline2WSException e) {
			return null;
		}
	}

}
