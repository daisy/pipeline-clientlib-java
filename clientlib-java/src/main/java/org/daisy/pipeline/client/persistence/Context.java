package org.daisy.pipeline.client.persistence;

import java.io.File;

public interface Context { // TODO: rename to JobContext
	
	// TODO: consider adding something like these to harmonize with JobContext.java
	public XProcInput getInputs();
    public XProcOutput getOutputs();
    public URI getLogFile();
    public XProcScript getScript();
    public JobId getId();
    public JobResultSet getResults();
    public void writeResult(XProcResult result);
    public String getName();
    public Client getClient();
    // TODO: note to self: consider using the context to bind together the job and the script; and add a validate() method to the context

	/**
	 * Add the file to the context.
	 * 
	 * @param file
	 * @param argument
	 */
	public void addFile(File file, String contextPath);
	
	/**
	 * Remove the file from the context.
	 * 
	 * The file will also be removed from all attached arguments. 
	 * 
	 * @param file
	 * @param argument
	 */
	public void removeFile(File file);
	
	/**
	 * Get the file associated with the given context path.
	 * 
	 * @param contextPath the context path for the file
	 * @return the context file with the given path
	 */
	public File getFile(String contextPath);
	
	/**
	 * Get the path in context associated with the File.
	 * 
	 * @param file the File object
	 * @return the associated path as a string
	 */
	public String getPath(File file);
	
	/**
	 * Bundles all context files up as a ZIP archive and returns it.
	 * @return the zip file
	 */
	public File getZip();
	
	/**
	 * Test whether the given path exists in the context.
	 * 
	 * The path must be relative and can refer to either a file or a directory. 
	 * 
	 * @param contextPath
	 * @return true if the path exists as either a file or directory in the context.
	 */
	public boolean exists(String contextPath);
	
	/**
	 * Test whether the given path exists as a file in the context.
	 * 
	 * @param contextPath
	 * @return true if the path exists as a file in the context.
	 */
	public boolean isFile(String contextPath);
	
	/**
	 * Test whether the given path exists as a directory in the context.
	 * 
	 * @param contextPath
	 * @return true if the path exists as a directory in the context.
	 */
	public boolean isDirectory(String contextPath);
	
}
