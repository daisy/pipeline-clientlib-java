package org.daisy.pipeline.client.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class DP2HttpJobMessagesCallbackResource extends ServerResource {  

	@Post("xml")  
	public void accept(Representation request) {
		try {
			Status status = Status.SUCCESS_OK;
			Pipeline2WSResponse response = new Pipeline2WSResponse(null, status.getCode(), status.getName(), status.getDescription(), request==null?null:request.getMediaType().toString(), request.getStream());
			setStatus(status);
			if (Pipeline2WS.callbackHandler != null)
				Pipeline2WS.callbackHandler.jobMessages(response);
			
		} catch (IOException e) {
			Pipeline2WS.logger().warn("Job messages callback: Could not parse request");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			Pipeline2WS.logger().debug(sw.toString());
		}
	}
}