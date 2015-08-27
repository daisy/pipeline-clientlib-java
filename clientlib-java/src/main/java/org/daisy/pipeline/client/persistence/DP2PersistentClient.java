package org.daisy.pipeline.client.persistence;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.daisy.pipeline.client.models.Job;
import org.w3c.dom.Document;

public interface DP2PersistentClient {

	/**
	 * Set the value for the given option or input to the provided boolean value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, boolean value);

	/**
	 * Set the value for the given option or input to the provided int value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, int value);

	/**
	 * Set the value for the given option or input to the provided double value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, double value);

	/**
	 * Set the value for the given option or input to the provided value.
	 * 
	 * Same as invoking set(String name, File fileOrDirectory, String contextPath)
	 * with contextPath set to null.
	 * 
	 * @param name name of the option or input
	 * @param fileOrDirectorty the file or directory
	 */
	public void set(String name, File fileOrDirectory);

	/**
	 * Set a file to be used as the value for the given option or input.
	 * 
	 * If this is a result-directory or temp-directory, the full file: URI to the directory
	 * will be used.
	 * 
	 * Otherwise, the fileOrDirectory will be stored as a path relative to the
	 * context directory.
	 * - if the contextPath parameter is not null, then that will be used as the path to
	 *   the file inside the context directory.
	 * - if the file is located inside the context directory, then a relative path to that
	 *   file will be inferred.
	 * - otherwise the file will be copied to the context directory and the context path
	 *   will be set to the filename of the file.
	 * 
	 * If contextPath ends with a slash ("/") and fileOrDirectory is a file, then the
	 * name of the file will be appended to the context path.
	 * 
	 * The context path will be canonicalized (removing stuff like "/./" and "/../")
	 * and will ensure that if fileOrDirectory is a directory then the path ends with a slash.
	 * 
	 * If the file parameter is null or points to a file or directory that does not exist,
	 * then the result is the same as invoking `clear(...)`.
	 * 
	 * @param name name of the option or input
	 * @param fileOrDirectorty the file or directory
	 * @param contextPath the relative path to use inside the context
	 */
	public void set(String name, File fileOrDirectory, String contextPath);

	/**
	 * Set the value for the given option or input to the provided string value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, String value);

	/**
	 * Set the values for the given option or input to the values in the provided list.
	 * 
	 * @param name name of the option or input
	 * @param values the values to use
	 */
	public void set(String name, List<String> values);

	/**
	 * Append a boolean value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, boolean value);

	/**
	 * Append a int value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, int value);

	/**
	 * Append a double value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, double value);

	/**
	 * Append a file to the end of the list of files for the given option or input.
	 * 
	 * If this is a result-directory or temp-directory, the full file: URI to the directory
	 * will be used.
	 * 
	 * Otherwise, the fileOrDirectory will be stored as a path relative to the
	 * context directory.
	 * - if the contextPath parameter is not null, then that will be used as the path to
	 *   the file inside the context directory.
	 * - if the file is located inside the context directory, then a relative path to that
	 *   file will be inferred.
	 * - otherwise the file will be copied to the context directory and the context path
	 *   will be set to the filename of the file.
	 * 
	 * If contextPath ends with a slash ("/") and fileOrDirectory is a file, then the
	 * name of the file will be appended to the context path.
	 * 
	 * The context path will be canonicalized (removing stuff like "/./" and "/../")
	 * and will ensure that if fileOrDirectory is a directory then the path ends with a slash.
	 * 
	 * If the file parameter is null or points to a file or directory that does not exist,
	 * then this method does nothing.
	 * 
	 * @param name name of the option or input
	 * @param fileOrDirectorty the file or directory
	 * @param contextPath the relative path to use inside the context
	 */
	public void add(String name, File fileOrDirectory, String contextPath);

	/**
	 * Append a file to the end of the list of files for the given option or input.
	 * 
	 * Same as invoking add(String name, File fileOrDirectory, String contextPath)
	 * with contextPath set to null.
	 * 
	 * @param name name of the option or input
	 * @param fileOrDirectorty the file or directory
	 */
	public void add(String name, File fileOrDirectory);

	/**
	 * Append a string value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, String value);

	/**
	 * Get the option value as a string.
	 * 
	 * If there are more than one value for the option or input, only the first one will be returned.
	 * If the option or input is a sequence, you should use {@link #getAsList(name) getAsList} to get all values instead.
	 * 
	 * Will return null if the option is undefined.
	 * 
	 * @param name option name
	 * @return the value as a string
	 */
	public String get(String name);

	/**
	 * Get the option value as a string.
	 * 
	 * If there are more than one value for the option or input, only the first one will be returned.
	 * If the option or input is a sequence, you should use {@link #getAsList(name) getAsList} to get all values instead.
	 * 
	 * @param name option name
	 * @param defaultValue default value if value is null or undefined
	 * @return the value as a string
	 */
	public String get(String name, String defaultValue);

	/**
	 * Get the option value as a boolean.
	 * 
	 * If there are more than one value for the option or input, only the first one will be returned.
	 * If the option or input is a sequence, you should use {@link #getAsBooleanList(name, defaultValue) getAsBooleanList} to get all values instead.
	 * 
	 * @param name option name
	 * @param defaultValue default value if value is null, undefined or unparseable
	 * @return the value as a boolean
	 */
	public boolean getAsBoolean(String name, boolean defaultValue);

	/**
	 * Get the option value as a int.
	 * 
	 * If there are more than one value for the option or input, only the first one will be returned.
	 * If the option or input is a sequence, you should use {@link #getAsIntegerList(name, defaultValue) getAsIntegerList} to get all values instead.
	 * 
	 * @param name option name
	 * @param defaultValue default value if value is null, undefined or unparseable
	 * @return the value as a int
	 */
	public int getAsInteger(String name, int defaultValue);

	/**
	 * Get the option value as a double.
	 * 
	 * If there are more than one value for the option or input, only the first one will be returned.
	 * If the option or input is a sequence, you should use {@link #getAsDoubleList(name, defaultValue) getAsDoubleList} to get all values instead.
	 * 
	 * @param name option name
	 * @param defaultValue default value if value is null, undefined or unparseable
	 * @return the value as a double
	 */
	public double getAsDouble(String name, double defaultValue);

	/**
	 * Get the option values as an list of strings.
	 * 
	 * This method will never return null.
	 * An empty list will be returned if the option is undefined.
	 * 
	 * @param name option name
	 * @return the string list
	 */
	public List<String> getAsList(String name);

	/**
	 * Get the option values as an list of booleans.
	 * 
	 * @param name option name
	 * @param defaultValue default value to use for unparseable values
	 * @return the boolean list
	 */
	public List<Boolean> getAsBooleanList(String name, boolean defaultValue);

	/**
	 * Get the option values as an list of ints.
	 * 
	 * @param name option name
	 * @param defaultValue default value to use for unparseable values
	 * @return the int list
	 */
	public List<Integer> getAsIntegerList(String name, int defaultValue);

	/**
	 * Get the option values as an list of doubles.
	 * 
	 * @param name option name
	 * @param defaultValue default value to use for unparseable values
	 * @return the double list
	 */
	public List<Double> getAsDoubleList(String name, double defaultValue);

	/**
	 * Unset the given option or input.
	 * 
	 * This is different from clearing the option in that it will no longer be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 * 
	 * @param name name of option or input
	 */
	public void unset(String name);

	/**
	 * Clear the given option or input.
	 * 
	 * This is different from unsetting the option in that it will still be defined.
	 * 
	 * An option that is cleared but not unset is submitted as an empty list of
	 * values to the Web API. An option that is unset are not submitted to the Web API,
	 * which leaves the Web API or the Pipeline 2 script free to use a default value.
	 * 
	 * @param name name of option or input
	 */
	public void clear(String name);

	/**
	 * Returns the set of names for the inputs and options.
	 * 
	 * @return number of values
	 */
	public Set<String> getNames();

	/**
	 * Returns the number of values defined for the option or input.
	 * 
	 * @param name the name of the option or input
	 * @return number of values
	 */
	public int getCount(String name);

	/**
	 * Get the script XML as a string.
	 * 
	 * @return XML document serialized as a string
	 */
	public String getScriptXml();

	/**
	 * Get the ID of the job request.
	 * 
	 * Note that this is not the same as the id you get after submitting a job request to the web api.
	 * This id is defined and used by the client application to manage its job client-side.
	 * 
	 * @return the id
	 */
	public String getId();

	/**
	 * Get the nicename of the job. Can be null.
	 * 
	 * @return the nicename
	 */
	public String getNicename();

	/**
	 * List all jobs that are stored in the given folder.
	 * 
	 * Jobs are lazy-loaded to improve performance.
	 * 
	 * @param jobStorage the directory where jobs are stored
	 * @return a list of all the jobs in the job storage
	 */
	public List<Job> listJobs(File jobStorage);

	/**
	 * Get the script href
	 * 
	 * @return the script href
	 */
	public String getScriptHref();

	/**
	 * Get the script id.
	 * 
	 * This is the last part of the URL, for instance http://.../scripts/script-id
	 * 
	 * @return the script id
	 */
	public String getScriptId();

	/**
	 * Load the job with the given id from the given job storage.
	 * 
	 * The files jobRequest.xml and script.xml must exist in the `jobStorage`/`jobId` directory,
	 * otherwise null will be returned. Use createJob(jobId, jobStorage, scriptXml) to create a job.
	 * 
	 * @param jobId job id
	 * @param jobStorage job storage
	 * @return the job, or null if it does not exist
	 */
	public Job loadJob(String jobId, File jobStorage);

	/**
	 * Create a job with the given id in the given job storage.
	 * 
	 * No files will be written to disk until save() is invoked. It will not
	 * be possible to add context files until save() is invoked for the first time.
	 * 
	 * If there is already a job with the given id in the job storage, null will be returned.
	 * 
	 * If no script xml is provided, null will be returned.
	 * 
	 * @param jobId job id
	 * @param jobStorage job storage
	 * @param scriptXml the script xml as a string
	 * @return the job, or null if it is not possible to create the job.
	 */
	public Job createJob(String jobId, File jobStorage, String scriptXml);

	/**
	 * Same as createJob(String, File, String), except that this lets you provide
	 * the script xml as a document directly instead of serializing it as a string.
	 * 
	 * @param jobId job id
	 * @param jobStorage job storage
	 * @param scriptXml the script xml as a Document
	 * @return the job, or null if it is not possible to create the job.
	 */

	public Job createJob(String jobId, File jobStorage, Document scriptXml);

	/**
	 * Deletes the job from the jobStorage
	 */
	public void delete();

	/**
	 * Save the job to the job storage
	 */
	public void save();

	/**
	 * Set the template to use for the job.
	 * 
	 * A template consists of a set of default option values as
	 * well as a set of context files.
	 * 
	 * @param jobTemplate the job to use as a template
	 */
	public void setJobTemplate(Job jobTemplate);

	/**
	 * Clear the template to use for the job.
	 * 
	 * A template consists of a set of default option values as
	 * well as a set of context files. After clearing the
	 * template, the templates context files will be removed
	 * from the job. Option values not referring to the
	 * context files will not be changed.
	 */
	public void clearJobTemplate();

	/**
	 * Get the attributes for an input or option.
	 * 
	 * Attributes are: nicename, desc, required, sequence, ordered, type, mediatype and outputType.
	 * 
	 * @param name the name of the input or option
	 * @return a key/value-map containing attributes for the input or option
	 */
	public Map<String,String> getAttributes(String name);

	/**
	 * Check if the input or option is filled out properly.
	 * 
	 * For instance, if the input or option is not a sequence, there must be exactly one value defined.
	 * 
	 * @param name name of the input or option
	 * @return a message describing the error, or null if there is no error
	 */
	public String validate(String name);

	/**
	 * Check that all the inputs and options is filled out properly.
	 * 
	 * @return a message describing the error, or null if there is no error
	 */
	public String validate();

}