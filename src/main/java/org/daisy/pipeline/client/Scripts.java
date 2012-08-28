package org.daisy.pipeline.client;

public class Scripts {

	/**
	 * Get all scripts
	 * 
	 * HTTP 200 OK: Response body contains XML data
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse get(String endpoint, String username, String secret) throws Pipeline2WSException {
		return Pipeline2WS.get(endpoint, "/scripts", username, secret, null);
	}

	/**
	 * Get a single script
	 * 
	 * HTTP 200 OK: Response body contains XML data
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * HTTP 404 Not Found: Resource not found
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse get(String endpoint, String username, String secret, String id) throws Pipeline2WSException {
		return Pipeline2WS.get(endpoint, "/scripts/"+id, username, secret, null);
	}

}
