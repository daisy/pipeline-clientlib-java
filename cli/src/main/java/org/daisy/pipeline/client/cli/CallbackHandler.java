package org.daisy.pipeline.client.cli;

import org.daisy.pipeline.client.Pipeline2WSCallbackHandler;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;

public class CallbackHandler implements Pipeline2WSCallbackHandler {
	
	public void jobMessages(Pipeline2WSResponse jobMessages) {
		try {
			System.out.println("-- jobMessages --");
			System.out.println(jobMessages.asText());
		} catch (Pipeline2WSException e) {
			e.printStackTrace(System.err);
		}
	}

	public void jobStatus(Pipeline2WSResponse jobStatus) {
		try {
			System.out.println("-- jobStatus --");
			System.out.println(jobStatus.asText());
		} catch (Pipeline2WSException e) {
			e.printStackTrace(System.err);
		}
	}
	
}