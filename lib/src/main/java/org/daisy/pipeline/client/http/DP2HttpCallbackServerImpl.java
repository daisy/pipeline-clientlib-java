package org.daisy.pipeline.client.http;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.daisy.pipeline.client.Pipeline2WS;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/**
 * Implementation of DP2HttpCallbackServer that uses Restlet as the underlying HTTP server.
 * 
 * @author jostein
 */
public class DP2HttpCallbackServerImpl extends Application implements DP2HttpCallbackServer {

	/** 
	 * Creates a root Restlet that will receive all incoming calls. 
	 */  
	@Override  
	public Restlet createInboundRoot() {
		// Create a router Restlet that defines routes.  
		Router router = new Router(getContext());  

		// Defines a route for the "jobmessages" resource  
		router.attach("/jobmessages", DP2HttpJobMessagesCallbackResource.class);  

		// Defines a route for the "jobstatus" resource  
		router.attach("/jobstatus", DP2HttpJobStatusCallbackResource.class);  

		return router;  
	}  

	private Component component;

	/**
	 * Inits the WS.
	 * @param port The port to start the web service on.
	 */
	public void init(int port) {
		Pipeline2WS.logger().info(String.format("Starting callback webservice on port %d", port));
		component = new Component();

		if (true) {
			component.getServers().add(Protocol.HTTP, port);
			Pipeline2WS.logger().debug("Using HTTP");

		} else {
			Pipeline2WS.logger().debug("Using HTTPS");
		}

		component.getDefaultHost().attach("", this);
		try {
			component.start();

		} catch (Exception e) {
			Pipeline2WS.logger().error("Callback webservice could not be started.");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Pipeline2WS.logger().error(sw.toString());
		}
	}

	/**
	 * Close.
	 * @throws Exception
	 * @throws Throwable
	 */
	public void close() throws Exception {
		if (this.component!=null)
			this.component.stop();
		this.stop();
		Pipeline2WS.logger().info("Callback webservice stopped.");

	}

}