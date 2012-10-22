package org.daisy.pipeline.client;

public interface Pipeline2WSCallbackHandler {
	
	/**
	 * Handle job message callbacks.
	 * @param jobMessages
	 */
	public void jobMessages(Pipeline2WSResponse jobMessages);
	
	/**
	 * Handle job status update callbacks.
	 * @param jobStatus
	 */
	public void jobStatus(Pipeline2WSResponse jobStatus);
	
}
