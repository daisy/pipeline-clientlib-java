package org.daisy.pipeline.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Methods for communicating with the "/scripts" resource in a Pipeline 2 Web Service.
 * 
 * @see http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI#Scripts
 * 
 * @author jostein
 */
public class Admin {

	/**
	 * Stop the web service
	 * 
	 * HTTP 204 No Content: Successfully processed the request, no content being returned
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * HTTP 403 Forbidden: The service could not be halted. This error would be caused by an invalid key.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse halt(String endpoint, String username, String secret, String key) throws Pipeline2WSException {
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("haltkey", key);
		return Pipeline2WS.get(endpoint, "/admin/halt", null, null, parameters);
	}

}
