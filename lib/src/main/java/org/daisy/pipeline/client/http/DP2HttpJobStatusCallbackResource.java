package org.daisy.pipeline.client.http;

import java.io.IOException;
import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class DP2HttpJobStatusCallbackResource extends ServerResource {  

	@Post("xml")  
	public void accept(Representation request) {
		try {
			Status status = Status.SUCCESS_OK;
			Pipeline2WSResponse response = new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), request==null?null:request.getMediaType().toString(), request.getStream());
			setStatus(status);
			if (Pipeline2WS.callbackHandler != null)
				Pipeline2WS.callbackHandler.jobStatus(response);
			
		} catch (IOException e) {
			if (Pipeline2WS.debug) {
				System.err.println("Could not parse request");
				e.printStackTrace(System.err);
			}
		}
	}
}