package org.daisy.pipeline.client.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.http.DP2HttpClient;
import org.w3c.dom.Document;

public class MockHttpClient implements DP2HttpClient {

	public Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException {
		return new Pipeline2WSResponse(200, "OK", "Mock object retrieved successfully", "application/xml", get(path));
	}
	
	public Pipeline2WSResponse delete(String endpoint, String path, String username, String secret, Map<String, String> parameters) throws Pipeline2WSException {
		return new Pipeline2WSResponse(200, "OK", "Mock object retrieved successfully", "application/xml", get(path));
	}
	
	public Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2WSException {
		return null;
	}
	
	public Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2WSException {
		return null;
	}
	
	/**
	 * Read mock response
	 * 
	 * @param path
	 * @return
	 */
	private InputStream get(String path) {
		File responseFile = new File("src/test/resources/responses"+path+".xml");
		Pipeline2WS.logger().info("Reading mock response: "+responseFile.getAbsolutePath());
		try {
			return new FileInputStream(responseFile);
		} catch (FileNotFoundException e) {
			Pipeline2WS.logger().info("Unable to read mock response for: "+path);
			e.printStackTrace();
			return null;
		}
	}

}
