package org.daisy.pipeline.client.http;

import java.io.File;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.w3c.dom.Document;

public interface DP2HttpClient {
	
	/**
	 * Send a GET request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parameters URL query string parameters
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException;
	
	/**
	 * POST an XML document.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param xml The XML document to post.
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2WSException;
	
	/**
	 * POST a multipart request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parts A map of all the parts.
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2WSException;

	/**
	 * Send a DELETE request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parameters URL query string parameters
	 * @return The return body.
	 * @throws Pipeline2WSException 
	 */
	public Pipeline2WSResponse delete(String endpoint, String path, String username, String secret, Map<String, String> parameters) throws Pipeline2WSException;
}