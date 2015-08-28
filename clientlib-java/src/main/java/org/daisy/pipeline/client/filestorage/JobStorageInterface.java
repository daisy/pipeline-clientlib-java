package org.daisy.pipeline.client.filestorage;

import java.io.File;

public interface JobStorageInterface {
	
	/**
	 * Save the job to the job storage.
	 *
	 * (i.e. stores the job XML)
	 */
	public abstract void save();
	
	/**
	 * Add the file to the context.
	 * 
	 * @param file
	 * @param argument
	 */
	public abstract void addContextFile(File file, String contextPath);

	/**
	 * Remove the file from the context.
	 * 
	 * The file will also be removed from all attached arguments. 
	 * 
	 * @param file
	 * @param argument
	 */
	public abstract void removeContextFile(String contextPath);

	/**
	 * Get the file associated with the given context path.
	 * 
	 * @param contextPath the context path for the file
	 * @return the context file with the given path
	 */
	public abstract File getContextFile(String contextPath);

	/**
	 * Get the path in context associated with the File.
	 * 
	 * @param file the File object
	 * @return the associated path as a string
	 */
	public abstract String getContextFilePath(File file);

	/**
	 * Returns the root directory for the context files.
	 * @return the zip file
	 */
	public abstract File getContextDir();
	
	/**
	 * Bundles all context files up as a ZIP archive and returns it.
	 * @return the zip file
	 */
	public abstract File getContextZip();

	/**
	 * Test whether the given path exists in the context.
	 * 
	 * The path must be relative and can refer to either a file or a directory. 
	 * 
	 * @param contextPath
	 * @return true if the path exists as either a file or directory in the context.
	 */
	public abstract boolean existsInContext(String contextPath);

	/**
	 * Test whether the given path exists as a file in the context.
	 * 
	 * @param contextPath
	 * @return true if the path exists as a file in the context.
	 */
	public abstract boolean isFileInContext(String contextPath);

	/**
	 * Test whether the given path exists as a directory in the context.
	 * 
	 * @param contextPath
	 * @return true if the path exists as a directory in the context.
	 */
	public abstract boolean isDirectoryInContext(String contextPath);

}