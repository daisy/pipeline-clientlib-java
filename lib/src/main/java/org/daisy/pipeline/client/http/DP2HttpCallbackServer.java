package org.daisy.pipeline.client.http;

public interface DP2HttpCallbackServer {
	
	/**
	 * Inits the WS.
	 * @param port The port to start the web service on.
	 */
	public void init(int port);
	
	/**
	 * Stops the web service.
	 * 
	 * @throws Exception
	 * @throws Throwable
	 */
	public void close() throws Exception;
	
}