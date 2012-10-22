package org.daisy.pipeline.client.http;


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
		if (Pipeline2WS.debug) System.out.println(String.format("Starting callback webservice on port %d", port));
		component = new Component();

		if (true) {
			component.getServers().add(Protocol.HTTP, port);
			if (Pipeline2WS.debug) System.out.println("Using HTTP");

		} else {
			if (Pipeline2WS.debug) System.out.println("Using HTTPS");
		}

		component.getDefaultHost().attach("", this);
		try {
			component.start();

		} catch (Exception e) {
			if (Pipeline2WS.debug) System.err.println("Callback webservice not started because of: "+e.getMessage());
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
		if (Pipeline2WS.debug) System.out.println("Callback webservice stopped.");

	}

}