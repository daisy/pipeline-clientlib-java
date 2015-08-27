package org.daisy.pipeline.client.http;

import java.io.File;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.utils.XML;
import org.w3c.dom.Document;

public class DP2HttpClientDummyImpl implements DP2HttpClient {

	public Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		return getDelete("GET", endpoint, path, username, secret, parameters);
	}

	public Pipeline2WSResponse delete(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		return getDelete("DELETE", endpoint, path, username, secret, parameters);
	}

	private Pipeline2WSResponse getDelete(String method, String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		String url = Pipeline2Client.url(endpoint, path, username, secret, parameters);
		if (endpoint == null) {
			return new Pipeline2WSResponse(url, 503, "Endpoint is not set", "Please provide a Pipeline 2 endpoint.", null, null, null);
		}

		Pipeline2Client.logger().warn(method+" "+url);

		throw new Pipeline2Exception("Error while "+method+"ing: No DP2HttpClient implementaion is set");
	}


	public Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2Exception {
		String url = Pipeline2Client.url(endpoint, path, username, secret, null);

		Pipeline2Client.logger().warn("POST "+url);
		Pipeline2Client.logger().warn(XML.toString(xml));

		throw new Pipeline2Exception("Error while POSTing: No DP2HttpClient implementaion is set");
	}


	public Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2Exception {
		String url = Pipeline2Client.url(endpoint, path, username, secret, null);

		Pipeline2Client.logger().warn("POST "+url+" (multipart)");

		throw new Pipeline2Exception("Error while POSTing: No DP2HttpClient implementaion is set");
	}

}