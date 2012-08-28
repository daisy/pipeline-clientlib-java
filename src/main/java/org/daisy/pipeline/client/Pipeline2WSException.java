package org.daisy.pipeline.client;

/**
 * A generic error thrown by the Pipeline 2 Client Library.
 * 
 * @author jostein
 */
public class Pipeline2WSException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public Pipeline2WSException(String message, Throwable cause) {
		super(message, cause);
	}

	public Pipeline2WSException(String message) {
		super(message);
	}

	public Pipeline2WSException(Throwable cause) {
		super(cause);
	}
	
}
