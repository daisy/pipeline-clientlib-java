package org.daisy.pipeline.client.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DP2PersistentClientImpl implements DP2PersistentClient {

	// provided at runtime; needed for validation and serialization
	private Document scriptDocument = null;

	// file storage
	private File directory;
	private String id;

	// job request
	private String nicename;
	private String scriptHref;
	private String priority; // can be "low", "medium", "high" or undefined
	private Map<String,List<String>> optionsAndInputs = new HashMap<String,List<String>>();
	private Map<String,File> contextBuffer = new HashMap<String,File>();
	
	// script
	private String scriptId;
	private String scriptNicename;
	private String scriptDescription;
	private Map<String,Map<String,String>> optionsAndInputsAttributes = new HashMap<String,Map<String,String>>();
	
	private Job jobTemplate = null;

	private boolean lazyLoaded = false;
	
	private static final Map<String,String> ns;
	static {
		ns = new HashMap<String,String>();
		ns.put("d", "http://www.daisy.org/ns/pipeline/data");
	}
	
	/** No public constructor. Use loadJob and createJob instead. */
	private DP2PersistentClientImpl() {}
	
	/**
	 * Set the value for the given option or input to the provided boolean value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, boolean value) {
		set(name, ""+value);
	}

	/**
	 * Set the value for the given option or input to the provided int value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, int value) {
		set(name, ""+value);
	}

	/**
	 * Set the value for the given option or input to the provided double value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, double value) {
		set(name, ""+value);
	}

	/**
	 * Set the value for the given option or input to the provided value.
	 * 
	 * Same as invoking set(String name, File fileOrDirectory, String contextPath)
	 * with contextPath set to null.
	 * 
	 * @param name name of the option or input
	 * @param fileOrDirectorty the file or directory
	 */
	public void set(String name, File fileOrDirectory) {
		set(name, fileOrDirectory, null);
	}

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
	public void set(String name, File fileOrDirectory, String contextPath) {
		clear(name);
		add(name, fileOrDirectory, contextPath);
	}

	/**
	 * Set the value for the given option or input to the provided string value.
	 * 
	 * @param name name of the option or input
	 * @param value the value to use
	 */
	public void set(String name, String value) {
		lazyLoad();
		List<String> stringValues = new ArrayList<String>();
		stringValues.add(""+value);
		optionsAndInputs.put(name, stringValues);
	}

	/**
	 * Set the values for the given option or input to the values in the provided list.
	 * 
	 * @param name name of the option or input
	 * @param values the values to use
	 */
	public void set(String name, List<String> values) {
		lazyLoad();
		List<String> stringValues = new ArrayList<String>();
		for (String value : values) {
			stringValues.add(""+value);
		}
		optionsAndInputs.put(name, stringValues);
	}

	/**
	 * Append a boolean value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, boolean value) {
		add(name, ""+value);
	}

	/**
	 * Append a int value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, int value) {
		add(name, ""+value);
	}

	/**
	 * Append a double value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, double value) {
		add(name, ""+value);
	}
	
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
	public void add(String name, File fileOrDirectory, String contextPath) {
		if (fileOrDirectory == null) {
			return;
		}
		
		lazyLoad();
		assert(optionsAndInputsAttributes.get(name) != null);
		if (!"".equals(optionsAndInputsAttributes.get(name).get("outputType"))) {
			add(name, fileOrDirectory.toURI().normalize().toString());
			
		} else {
			
			if (contextPath == null) {
				contextPath = fileOrDirectory.getName();
			} else {
				try {
					URI uri = new URI(contextPath);
					contextPath = uri.normalize().toString();
				} catch (URISyntaxException e) {
					System.err.println("Unable to resolve URI: '"+contextPath+"'");
					e.printStackTrace();
				}
			}
			contextBuffer.put(contextPath, fileOrDirectory);
			storeFilesInContextIfPossible();
			
			add(name, contextPath);
		}
	}
	
	/**
	 * Append a file to the end of the list of files for the given option or input.
	 * 
	 * Same as invoking add(String name, File fileOrDirectory, String contextPath)
	 * with contextPath set to null.
	 * 
	 * @param name name of the option or input
	 * @param fileOrDirectorty the file or directory
	 */
	public void add(String name, File fileOrDirectory) {
		add(name, fileOrDirectory, null);
	}

	/**
	 * Append a string value to the end of the list of values for the given option or input.
	 * 
	 * @param name name of the option or input
	 * @param value the value to append
	 */
	public void add(String name, String value) {
		lazyLoad();
		if (optionsAndInputs.get(name) == null) {
			optionsAndInputs.put(name, new ArrayList<String>());
		}

		optionsAndInputs.get(name).add(value);
	}

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
	public String get(String name) {
		return get(name, null);
	}

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
	public String get(String name, String defaultValue) {
		lazyLoad();
		if (optionsAndInputs.get(name) == null) {
			return defaultValue;
		} else {
			return optionsAndInputs.get(name).get(0);
		}
	}

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
	public boolean getAsBoolean(String name, boolean defaultValue) {
		lazyLoad();
		String value = get(name);
		if (value != null && ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()))) {
			return Boolean.parseBoolean(get(name));

		} else {
			return defaultValue;
		}
	}

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
	public int getAsInteger(String name, int defaultValue) {
		lazyLoad();
		try {
			return Integer.parseInt(get(name));

		} catch (Exception e) {
			return defaultValue;
		}
	}

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
	public double getAsDouble(String name, double defaultValue) {
		lazyLoad();
		try {
			return Double.parseDouble(get(name));

		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the option values as an list of strings.
	 * 
	 * This method will never return null.
	 * An empty list will be returned if the option is undefined.
	 * 
	 * @param name option name
	 * @return the string list
	 */
	public List<String> getAsList(String name) {
		lazyLoad();
		if (optionsAndInputs.get(name) != null) {
			return optionsAndInputs.get(name);

		} else {
			return new ArrayList<String>();
		}
	}

	/**
	 * Get the option values as an list of booleans.
	 * 
	 * @param name option name
	 * @param defaultValue default value to use for unparseable values
	 * @return the boolean list
	 */
	public List<Boolean> getAsBooleanList(String name, boolean defaultValue) {
		lazyLoad();
		if (optionsAndInputs.get(name) != null) {
			List<String> list = optionsAndInputs.get(name);
			List<Boolean> booleanList = new ArrayList<Boolean>();
			for (String value : list) {
				if (value != null && ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()))) {
					booleanList.add(new Boolean(Boolean.parseBoolean(get(name))));

				} else {
					booleanList.add(new Boolean(defaultValue));
				}
			}
			return booleanList;

		} else {
			return new ArrayList<Boolean>();
		}
	}

	/**
	 * Get the option values as an list of ints.
	 * 
	 * @param name option name
	 * @param defaultValue default value to use for unparseable values
	 * @return the int list
	 */
	public List<Integer> getAsIntegerList(String name, int defaultValue) {
		lazyLoad();
		Boolean test = true;
		boolean foo = test;
		if (optionsAndInputs.get(name) != null) {
			List<String> list = optionsAndInputs.get(name);
			List<Integer> intList = new ArrayList<Integer>();
			for (String value : list) {
				try {
					intList.add(Integer.parseInt(value));

				} catch (Exception e) {
					intList.add(defaultValue);
				}
			}
			return intList;

		} else {
			return new ArrayList<Integer>();
		}
	}

	/**
	 * Get the option values as an list of doubles.
	 * 
	 * @param name option name
	 * @param defaultValue default value to use for unparseable values
	 * @return the double list
	 */
	public List<Double> getAsDoubleList(String name, double defaultValue) {
		lazyLoad();
		if (optionsAndInputs.get(name) != null) {
			List<String> list = optionsAndInputs.get(name);
			List<Double> doubleList = new ArrayList<Double>();
			for (String value : list) {
				try {
					doubleList.add(new Double(Double.parseDouble(value)));

				} catch (Exception e) {
					doubleList.add(defaultValue);
				}
			}
			return doubleList;

		} else {
			return new ArrayList<Double>();
		}
	}

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
	public void unset(String name) {
		lazyLoad();
		optionsAndInputs.put(name, null);
	}
	
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
	public void clear(String name) {
		lazyLoad();
		if (optionsAndInputs.get(name) != null) {
			optionsAndInputs.get(name).clear();
		}
	}

	/**
	 * Returns the set of names for the inputs and options.
	 * 
	 * @return number of values
	 */
	public Set<String> getNames() {
		lazyLoad();
		return optionsAndInputs.keySet();
	}

	/**
	 * Returns the number of values defined for the option or input.
	 * 
	 * @param name the name of the option or input
	 * @return number of values
	 */
	public int getCount(String name) {
		lazyLoad();
		if (optionsAndInputs.get(name) == null) {
			return 0;

		} else {
			return optionsAndInputs.get(name).size();
		}
	}

	/**
	 * Get the script XML as a string.
	 * 
	 * @return XML document serialized as a string
	 */
	public String getScriptXml() {
		lazyLoad();
		return XML.toString(scriptDocument);
	}

	/**
	 * Get the ID of the job request.
	 * 
	 * Note that this is not the same as the id you get after submitting a job request to the web api.
	 * This id is defined and used by the client application to manage its job client-side.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the nicename of the job. Can be null.
	 * 
	 * @return the nicename
	 */
	public String getNicename() {
		lazyLoad();
		return nicename;
	}

	private void lazyLoad() {
		if (lazyLoaded) {
			return;
		}

		if (directory != null && directory.exists()) {
			storeFilesInContextIfPossible();
			
			File jobRequestFile = new File(directory, "jobRequest.xml");
			String jobRequestString = null;
			Document jobRequestDocument = null;
			if (jobRequestFile.exists()) {
				try {
					byte[] encoded = Files.readAllBytes(Paths.get(jobRequestFile.getPath()));
					jobRequestString = new String(encoded, Charset.defaultCharset());
					jobRequestDocument = XML.getXml(jobRequestString);

				} catch (IOException e) {
					System.err.println("Unable to load jobRequest.xml: "+jobRequestFile.getAbsolutePath());
					e.printStackTrace();
				}

				if (jobRequestDocument != null) {
					try {
						scriptHref = XPath.selectText("/*/d:script/@href", jobRequestDocument, ns);
						nicename = XPath.selectText("/*/d:nicename", jobRequestDocument, ns);
						priority = XPath.selectText("/*/d:priority", jobRequestDocument, ns);

						for (Node option : XPath.selectNodes("/*/d:option", jobRequestDocument, ns)) {
							String name = XPath.selectText("@name", option, ns);
							optionsAndInputs.put(name, new ArrayList<String>());
							List<Node> optionItems = XPath.selectNodes("d:item", option, ns);
							if (optionItems.isEmpty()) {
								String value = XPath.selectText(".", option, ns);
								if (value != null && !"".equals(value)) {
									optionsAndInputs.get(name).add(value);
								}

							} else {
								for (Node optionItem : optionItems) {
									String value = XPath.selectText("@value", optionItem, ns);
									optionsAndInputs.get(name).add(value);
								}
							}
						}

						for (Node input : XPath.selectNodes("/*/d:input", jobRequestDocument, ns)) {
							// NOTE: docwrapper not used in the jobRequest.xml file
							String name = XPath.selectText("@name", input, ns);
							optionsAndInputs.put(name, new ArrayList<String>());
							for (Node optionItem : XPath.selectNodes("d:item", input, ns)) {
								String value = XPath.selectText("@value", optionItem, ns);
								optionsAndInputs.get(name).add(value);
							}
						}

					} catch (Pipeline2Exception e) {
						System.err.println("Failed to parse jobRequest.xml: "+jobRequestFile.getAbsolutePath());
						e.printStackTrace();
					}
				}
			}
			
			File scriptFile = new File(directory, "script.xml");
			if (scriptFile.exists()) {
				try {
					byte[] encoded = Files.readAllBytes(Paths.get(scriptFile.getPath()));
					String scriptXml = new String(encoded, Charset.defaultCharset());
					scriptDocument = XML.getXml(scriptXml);

				} catch (IOException e) {
					System.err.println("Unable to load script.xml: "+scriptFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
		assert(scriptDocument != null);

		try {
			scriptId = XPath.selectText("/*/@id", scriptDocument, ns);
			scriptDescription = XPath.selectText("/*/d:description", scriptDocument, ns);
			scriptNicename = XPath.selectText("/*/d:nicename", scriptDocument, ns);

			for (Node option : XPath.selectNodes("/*/d:option", scriptDocument, ns)) {
				String name = XPath.selectText("@name", option, ns);
				optionsAndInputsAttributes.put(name, new HashMap<String,String>());
				optionsAndInputsAttributes.get(name).put("kind", "option");
				optionsAndInputsAttributes.get(name).put("nicename", XPath.selectText("@nicename", option, ns));
				optionsAndInputsAttributes.get(name).put("desc", XPath.selectText("@desc", option, ns));
				optionsAndInputsAttributes.get(name).put("required", ""+"true".equals(XPath.selectText("@required", option, ns)));
				optionsAndInputsAttributes.get(name).put("sequence", ""+"true".equals(XPath.selectText("@sequence", option, ns)));
				optionsAndInputsAttributes.get(name).put("ordered", ""+"true".equals(XPath.selectText("@ordered", option, ns)));
				optionsAndInputsAttributes.get(name).put("type", XPath.selectText("@type", option, ns));
				optionsAndInputsAttributes.get(name).put("mediatype", XPath.selectText("@mediatype", option, ns));
				optionsAndInputsAttributes.get(name).put("outputType", XPath.selectText("@outputType", option, ns));
			}

			for (Node input : XPath.selectNodes("/*/d:input", scriptDocument, ns)) {
				String name = XPath.selectText("@name", input, ns);
				optionsAndInputsAttributes.put(name, new HashMap<String,String>());
				optionsAndInputsAttributes.get(name).put("kind", "input");
				optionsAndInputsAttributes.get(name).put("nicename", XPath.selectText("@nicename", input, ns));
				optionsAndInputsAttributes.get(name).put("desc", XPath.selectText("@desc", input, ns));
				optionsAndInputsAttributes.get(name).put("required", ""+"true".equals(XPath.selectText("@required", input, ns)));
				optionsAndInputsAttributes.get(name).put("sequence", ""+"true".equals(XPath.selectText("@sequence", input, ns)));
				optionsAndInputsAttributes.get(name).put("ordered", "true"); // implied
				optionsAndInputsAttributes.get(name).put("type", "anyFileURI"); // implied
				optionsAndInputsAttributes.get(name).put("mediatype", XPath.selectText("@mediatype", input, ns));
				optionsAndInputsAttributes.get(name).put("outputType", ""); // implied
			}

		} catch (Pipeline2Exception e) {
			System.err.println("Failed to parse script.xml for job with id='"+id+"'");
			e.printStackTrace();
		}
		lazyLoaded = true;
	}
	
	private void storeFilesInContextIfPossible() {
		if (directory != null && directory.exists()) {
			if (!contextBuffer.isEmpty()) {
				for (String contextPath : contextBuffer.keySet()) {
					File fileOrDirectory = contextBuffer.get(contextPath);
					contextBuffer.remove(contextPath);

					if (!fileOrDirectory.exists()) {
						System.err.println("File or directory does not exist and can not be added to context: '"+fileOrDirectory.getAbsolutePath()+"'");
						continue;
					}

					File contextDir = new File(directory, "context");
					File contextFileOrDirectory;
					if (contextPath == null) {
						contextFileOrDirectory = new File(contextDir, fileOrDirectory.getName());
					} else if (contextPath.endsWith("/")) {
						contextFileOrDirectory = new File(new File(contextDir, contextPath), fileOrDirectory.getName());
					} else {
						contextFileOrDirectory = new File(contextDir, contextPath);
					}
					if (fileOrDirectory.isDirectory()) {
						contextFileOrDirectory.mkdirs();
					}
					try {
						assert contextFileOrDirectory.getCanonicalPath().startsWith(contextDir.getCanonicalPath() + File.separator); // contextFile is inside contextDir

						if (!fileOrDirectory.getCanonicalPath().equals(contextFileOrDirectory.getCanonicalPath())) {
							org.daisy.pipeline.client.utils.Files.copy(fileOrDirectory, contextFileOrDirectory);
						}

					} catch (IOException e) {
						System.err.println("Unable to copy from '"+fileOrDirectory.getAbsolutePath()+"' to '"+contextFileOrDirectory.getAbsolutePath());
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * List all jobs that are stored in the given folder.
	 * 
	 * Jobs are lazy-loaded to improve performance.
	 * 
	 * @param jobStorage the directory where jobs are stored
	 * @return a list of all the jobs in the job storage
	 */
	public List<Job> listJobs(File jobStorage) {
		List<Job> jobs = new ArrayList<Job>();
		if (jobStorage.isDirectory()) {
			for (File directory : jobStorage.listFiles()) {
				if (directory.isDirectory()) {
					String jobId = directory.getName();
					jobs.add(loadJob(jobId, jobStorage));
				}
			}
		}
		Collections.sort(jobs);
		return jobs;
	}

	/**
	 * Get the script href
	 * 
	 * @return the script href
	 */
	public String getScriptHref() {
		return scriptHref;
	}

	/**
	 * Get the script id.
	 * 
	 * This is the last part of the URL, for instance http://.../scripts/script-id
	 * 
	 * @return the script id
	 */
	public String getScriptId() {
		if (scriptId == null && scriptHref != null) {
			String[] parts = scriptHref.split("/");
			scriptId = parts[parts.length-1];
			return scriptId;

		}
		return scriptId;
	}

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
	public Job loadJob(String jobId, File jobStorage) {
		File jobDir = new File(jobStorage, jobId);
		if (jobDir.isDirectory() && new File(jobDir, "jobRequest.xml").isFile() && new File(jobDir, "script.xml").isFile()) {
			Job job = new Job();
			job.id = jobId;
			job.directory = jobDir;
			return job;
			
		} else {
			return null;
		}
	}
	
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
	public static Job createJob(String jobId, File jobStorage, String scriptXml) {
		return createJob(jobId, jobStorage, XML.getXml(scriptXml));
	}
	
	/**
	 * Same as createJob(String, File, String), except that this lets you provide
	 * the script xml as a document directly instead of serializing it as a string.
	 * 
	 * @param jobId job id
	 * @param jobStorage job storage
	 * @param scriptXml the script xml as a Document
	 * @return the job, or null if it is not possible to create the job.
	 */
	
	public static Job createJob(String jobId, File jobStorage, Document scriptXml) {
		File jobDir = new File(jobStorage, jobId);
		if (!jobDir.isDirectory()) {
			Job job = new Job();
			job.id = jobId;
			job.directory = jobDir;
			job.scriptDocument = scriptXml;
			return job;
			
		} else {
			return null;
		}
	}

	/**
	 * Deletes the job from the jobStorage
	 */
	public void delete() {
		if (directory != null && directory.exists()) {
			try {
				deleteRecursively(directory);
			} catch (IOException e) {
				System.err.println("Unable to delete job: "+directory.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
	
	private void deleteRecursively(File directory) throws IOException {
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				deleteRecursively(file);
			}
		}
		Files.delete(directory.toPath());
	}
	
	/**
	 * Save the job to the job storage
	 */
	public void save() {
		if (scriptDocument == null) {
			System.err.println("Script XML not specified; unable to serialize inputs and options");
			return;
		}
		
		File jobRequestFile = new File(directory, "jobRequest.xml");
		File scriptFile = new File(directory, "script.xml");
		Document jobRequestDocument = null;
		
		directory.mkdirs();
		lazyLoad();
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			jobRequestDocument = builder.newDocument();
			Element jobRequestElement = jobRequestDocument.createElementNS(ns.get("d"), "jobRequest");
			jobRequestDocument.appendChild(jobRequestElement);
			
			if (scriptHref != null) {
				Element scriptElement = jobRequestDocument.createElementNS(ns.get("d"), "script");
				scriptElement.setAttribute("href", scriptHref);
				jobRequestElement.appendChild(scriptElement);
			}
			
			if (nicename != null) {
				Element nicenameElement = jobRequestDocument.createElementNS(ns.get("d"), "nicename");
				nicenameElement.setTextContent(nicename);
				jobRequestElement.appendChild(nicenameElement);
			}
			
			if (priority != null) {
				Element priorityElement = jobRequestDocument.createElementNS(ns.get("d"), "priority");
				priorityElement.setTextContent(priority);
				jobRequestElement.appendChild(priorityElement);
			}
			
			for (Node scriptInputNode : XPath.selectNodes("/*/d:input", scriptDocument, ns)) {
				String name = XPath.selectText("@name", scriptInputNode, ns);
				//String nicename = XPath.selectText("@nicename", scriptInputNode, ns);
				//String desc = XPath.selectText("@desc", scriptInputNode, ns);
				boolean required = "true".equals(XPath.selectText("@required", scriptInputNode, ns));
				//boolean sequence = "true".equals(XPath.selectText("@sequence", scriptInputNode, ns));
				//String mediaType = XPath.selectText("@mediaType", scriptInputNode, ns);
				
				if (required || optionsAndInputs.get(name) != null) {
					Element inputElement = jobRequestDocument.createElementNS(ns.get("d"), "input");
					
					List<String> inputValues = getAsList(name);
					for (String inputValue : inputValues) {
						Element itemElement = jobRequestDocument.createElementNS(ns.get("d"), "item");
						itemElement.setAttribute("value", inputValue);
						inputElement.appendChild(itemElement);
					}
					
					jobRequestElement.appendChild(inputElement);
				}
			}
			
			for (Node scriptOptionNode : XPath.selectNodes("/*/d:option", scriptDocument, ns)) {
				String name = XPath.selectText("@name", scriptOptionNode, ns);
				//String nicename = XPath.selectText("@nicename", scriptOptionNode, ns);
				//String desc = XPath.selectText("@desc", scriptOptionNode, ns);
				boolean required = "true".equals(XPath.selectText("@required", scriptOptionNode, ns));
				boolean sequence = "true".equals(XPath.selectText("@sequence", scriptOptionNode, ns));
				//boolean ordered = "true".equals(XPath.selectText("@ordered", scriptOptionNode, ns));
				//String type = XPath.selectText("@type", scriptOptionNode, ns);
				//String mediaType = XPath.selectText("@mediaType", scriptOptionNode, ns);
				//String mediaType = XPath.selectText("@outputType", scriptOptionNode, ns);
				
				if (required || optionsAndInputs.get(name) != null) {
					Element optionElement = jobRequestDocument.createElementNS(ns.get("d"), "option");
					
					if (sequence) {
						List<String> optionValues = getAsList(name);
						for (String optionValue : optionValues) {
							Element itemElement = jobRequestDocument.createElementNS(ns.get("d"), "item");
							itemElement.setAttribute("value", optionValue);
							optionElement.appendChild(itemElement);
						}
						
					} else {
						optionElement.setTextContent(get(name));
					}
					
					jobRequestElement.appendChild(optionElement);
				}
			}
			
			String jobRequestString = XML.toString(jobRequestDocument);
			Files.write(jobRequestFile.toPath(), jobRequestString.getBytes());
			
			String scriptString = XML.toString(scriptDocument);
			Files.write(scriptFile.toPath(), scriptString.getBytes());
			
			File contextDir = new File(directory, "context");
			contextDir.mkdirs();
			
			
		} catch (ParserConfigurationException e) {
			System.err.println("Unable to create jobRequest document for job with id '"+id+"'");
			e.printStackTrace();
			
		} catch (XPathExpressionException e) {
			System.err.println("Unable to parse provided script XML");
			e.printStackTrace();
			
		} catch (IOException e) {
			System.err.println("Unable to store XML for job");
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the template to use for the job.
	 * 
	 * A template consists of a set of default option values as
	 * well as a set of context files.
	 * 
	 * @param jobTemplate the job to use as a template
	 */
	public void setJobTemplate(Job jobTemplate) {
		this.jobTemplate = jobTemplate;
	}
	
	/**
	 * Clear the template to use for the job.
	 * 
	 * A template consists of a set of default option values as
	 * well as a set of context files. After clearing the
	 * template, the templates context files will be removed
	 * from the job. Option values not referring to the
	 * context files will not be changed.
	 */
	public void clearJobTemplate() {
		this.jobTemplate = null;
	}
	
	/**
	 * Get the attributes for an input or option.
	 * 
	 * Attributes are: nicename, desc, required, sequence, ordered, type, mediatype and outputType.
	 * 
	 * @param name the name of the input or option
	 * @return a key/value-map containing attributes for the input or option
	 */
	public Map<String,String> getAttributes(String name) {
		return optionsAndInputsAttributes.get(name);
	}

	// Compile some patterns for validating certain XML types
	private static final String NameStartChar = "[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF"+
												"\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD"+
												"\\u10000-\\uEFFFF]";
    private static final String NameChar = NameStartChar+"\\-\\.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040";
    private static final String Name = "["+NameStartChar+"]["+NameChar+"]*";
    private static final String BaseChar = "\\u0041-\\u005A\\u0061-\\u007A\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u00FF\\u0100-\\u0131"+
    										"\\u0134-\\u013E\\u0141-\\u0148\\u014A-\\u017E\\u0180-\\u01C3\\u01CD-\\u01F0\\u01F4-\\u01F5"+
											"\\u01FA-\\u0217\\u0250-\\u02A8\\u02BB-\\u02C1\\u0386\\u0388-\\u038A\\u038C\\u038E-\\u03A1"+
											"\\u03A3-\\u03CE\\u03D0-\\u03D6\\u03DA\\u03DC\\u03DE\\u03E0\\u03E2-\\u03F3\\u0401-\\u040C"+
											"\\u040E-\\u044F\\u0451-\\u045C\\u045E-\\u0481\\u0490-\\u04C4\\u04C7-\\u04C8\\u04CB-\\u04CC"+
											"\\u04D0-\\u04EB\\u04EE-\\u04F5\\u04F8-\\u04F9\\u0531-\\u0556\\u0559\\u0561-\\u0586"+
											"\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0621-\\u063A\\u0641-\\u064A\\u0671-\\u06B7\\u06BA-\\u06BE"+
											"\\u06C0-\\u06CE\\u06D0-\\u06D3\\u06D5\\u06E5-\\u06E6\\u0905-\\u0939\\u093D\\u0958-\\u0961"+
											"\\u0985-\\u098C\\u098F-\\u0990\\u0993-\\u09A8\\u09AA-\\u09B0\\u09B2\\u09B6-\\u09B9"+
											"\\u09DC-\\u09DD\\u09DF-\\u09E1\\u09F0-\\u09F1\\u0A05-\\u0A0A\\u0A0F-\\u0A10\\u0A13-\\u0A28"+
											"\\u0A2A-\\u0A30\\u0A32-\\u0A33\\u0A35-\\u0A36\\u0A38-\\u0A39\\u0A59-\\u0A5C\\u0A5E"+
											"\\u0A72-\\u0A74\\u0A85-\\u0A8B\\u0A8D\\u0A8F-\\u0A91\\u0A93-\\u0AA8\\u0AAA-\\u0AB0"+
											"\\u0AB2-\\u0AB3\\u0AB5-\\u0AB9\\u0ABD\\u0AE0\\u0B05-\\u0B0C\\u0B0F-\\u0B10\\u0B13-\\u0B28"+
											"\\u0B2A-\\u0B30\\u0B32-\\u0B33\\u0B36-\\u0B39\\u0B3D\\u0B5C-\\u0B5D\\u0B5F-\\u0B61"+
											"\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92-\\u0B95\\u0B99-\\u0B9A\\u0B9C\\u0B9E-\\u0B9F"+
											"\\u0BA3-\\u0BA4\\u0BA8-\\u0BAA\\u0BAE-\\u0BB5\\u0BB7-\\u0BB9\\u0C05-\\u0C0C\\u0C0E-\\u0C10"+
											"\\u0C12-\\u0C28\\u0C2A-\\u0C33\\u0C35-\\u0C39\\u0C60-\\u0C61\\u0C85-\\u0C8C\\u0C8E-\\u0C90"+
											"\\u0C92-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CDE\\u0CE0-\\u0CE1\\u0D05-\\u0D0C"+
											"\\u0D0E-\\u0D10\\u0D12-\\u0D28\\u0D2A-\\u0D39\\u0D60-\\u0D61\\u0E01-\\u0E2E\\u0E30"+
											"\\u0E32-\\u0E33\\u0E40-\\u0E45\\u0E81-\\u0E82\\u0E84\\u0E87-\\u0E88\\u0E8A\\u0E8D"+
											"\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA-\\u0EAB\\u0EAD-\\u0EAE"+
											"\\u0EB0\\u0EB2-\\u0EB3\\u0EBD\\u0EC0-\\u0EC4\\u0F40-\\u0F47\\u0F49-\\u0F69\\u10A0-\\u10C5"+
											"\\u10D0-\\u10F6\\u1100\\u1102-\\u1103\\u1105-\\u1107\\u1109\\u110B-\\u110C\\u110E-\\u1112"+
											"\\u113C\\u113E\\u1140\\u114C\\u114E\\u1150\\u1154-\\u1155\\u1159\\u115F-\\u1161\\u1163"+
											"\\u1165\\u1167\\u1169\\u116D-\\u116E\\u1172-\\u1173\\u1175\\u119E\\u11A8\\u11AB"+
											"\\u11AE-\\u11AF\\u11B7-\\u11B8\\u11BA\\u11BC-\\u11C2\\u11EB\\u11F0\\u11F9\\u1E00-\\u1E9B"+
											"\\u1EA0-\\u1EF9\\u1F00-\\u1F15\\u1F18-\\u1F1D\\u1F20-\\u1F45\\u1F48-\\u1F4D\\u1F50-\\u1F57"+
											"\\u1F59\\u1F5B\\u1F5D\\u1F5F-\\u1F7D\\u1F80-\\u1FB4\\u1FB6-\\u1FBC\\u1FBE\\u1FC2-\\u1FC4"+
											"\\u1FC6-\\u1FCC\\u1FD0-\\u1FD3\\u1FD6-\\u1FDB\\u1FE0-\\u1FEC\\u1FF2-\\u1FF4\\u1FF6-\\u1FFC"+
											"\\u2126\\u212A-\\u212B\\u212E\\u2180-\\u2182\\u3041-\\u3094\\u30A1-\\u30FA\\u3105-\\u312C"+
											"\\uAC00-\\uD7A3";
    private static final String Ideographic = "\\u4E00-\\u9FA5\\u3007\\u3021-\\u3029";
    private static final String Letter = BaseChar+Ideographic;
    private static final String Digit = "\\u0030-\\u0039\\u0660-\\u0669\\u06F0-\\u06F9\\u0966-\\u096F\\u09E6-\\u09EF\\u0A66-\\u0A6F"+
											"\\u0AE6-\\u0AEF\\u0B66-\\u0B6F\\u0BE7-\\u0BEF\\u0C66-\\u0C6F\\u0CE6-\\u0CEF\\u0D66-\\u0D6F"+
											"\\u0E50-\\u0E59\\u0ED0-\\u0ED9\\u0F20-\\u0F29";
    private static final String CombiningChar = "\\u0300-\\u0345\\u0360-\\u0361\\u0483-\\u0486\\u0591-\\u05A1\\u05A3-\\u05B9"+
											"\\u05BB-\\u05BD\\u05BF\\u05C1-\\u05C2\\u05C4\\u064B-\\u0652\\u0670\\u06D6-\\u06DC"+
											"\\u06DD-\\u06DF\\u06E0-\\u06E4\\u06E7-\\u06E8\\u06EA-\\u06ED\\u0901-\\u0903\\u093C"+
											"\\u093E-\\u094C\\u094D\\u0951-\\u0954\\u0962-\\u0963\\u0981-\\u0983\\u09BC\\u09BE\\u09BF"+
											"\\u09C0-\\u09C4\\u09C7-\\u09C8\\u09CB-\\u09CD\\u09D7\\u09E2-\\u09E3\\u0A02\\u0A3C\\u0A3E"+
											"\\u0A3F\\u0A40-\\u0A42\\u0A47-\\u0A48\\u0A4B-\\u0A4D\\u0A70-\\u0A71\\u0A81-\\u0A83\\u0ABC"+
											"\\u0ABE-\\u0AC5\\u0AC7-\\u0AC9\\u0ACB-\\u0ACD\\u0B01-\\u0B03\\u0B3C\\u0B3E-\\u0B43"+
											"\\u0B47-\\u0B48\\u0B4B-\\u0B4D\\u0B56-\\u0B57\\u0B82-\\u0B83\\u0BBE-\\u0BC2\\u0BC6-\\u0BC8"+
											"\\u0BCA-\\u0BCD\\u0BD7\\u0C01-\\u0C03\\u0C3E-\\u0C44\\u0C46-\\u0C48\\u0C4A-\\u0C4D"+
											"\\u0C55-\\u0C56\\u0C82-\\u0C83\\u0CBE-\\u0CC4\\u0CC6-\\u0CC8\\u0CCA-\\u0CCD\\u0CD5-\\u0CD6"+
											"\\u0D02-\\u0D03\\u0D3E-\\u0D43\\u0D46-\\u0D48\\u0D4A-\\u0D4D\\u0D57\\u0E31\\u0E34-\\u0E3A"+
											"\\u0E47-\\u0E4E\\u0EB1\\u0EB4-\\u0EB9\\u0EBB-\\u0EBC\\u0EC8-\\u0ECD\\u0F18-\\u0F19\\u0F35"+
											"\\u0F37\\u0F39\\u0F3E\\u0F3F\\u0F71-\\u0F84\\u0F86-\\u0F8B\\u0F90-\\u0F95\\u0F97"+
											"\\u0F99-\\u0FAD\\u0FB1-\\u0FB7\\u0FB9\\u20D0-\\u20DC\\u20E1\\u302A-\\u302F\\u3099\\u309A";
    private static final String Extender = "\\u00B7\\u02D0\\u02D1\\u0387\\u0640\\u0E46\\u0EC6\\u3005"+
											"\\u3031-\\u3035\\u309D-\\u309E\\u30FC-\\u30FE";
    private static final String NCNameChar = Letter+Digit+"\\.\\-_"+CombiningChar+Extender;
	private static final String NCName = "["+Letter+"_]["+NCNameChar+"]*";
	private static final String ID = Name;
	private static final String IDREF = Name;
	private static final String ENTITY = Name;
	private static final String Nmtoken = "["+NameChar+"]+";
	private static final String QName = "(["+NCName+"]:)?"+NCName;
	private static final String language = "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*";
	private static final Pattern languagePattern = Pattern.compile(language, Pattern.CANON_EQ);
	private static final Pattern namePattern = Pattern.compile(Name, Pattern.CANON_EQ);
	private static final Pattern ncnamePattern = Pattern.compile(NCName, Pattern.CANON_EQ);
	private static final Pattern idPattern = Pattern.compile(ID, Pattern.CANON_EQ);
	private static final Pattern idrefPattern = Pattern.compile(IDREF, Pattern.CANON_EQ);
	private static final Pattern entityPattern = Pattern.compile(ENTITY, Pattern.CANON_EQ);
	private static final Pattern nmtokenPattern = Pattern.compile(Nmtoken, Pattern.CANON_EQ);
	private static final Pattern qnamePattern = Pattern.compile(QName, Pattern.CANON_EQ);
	
	/**
	 * Check if the input or option is filled out properly.
	 * 
	 * For instance, if the input or option is not a sequence, there must be exactly one value defined.
	 * 
	 * @param name name of the input or option
	 * @return a message describing the error, or null if there is no error
	 */
	public String validate(String name) {
		lazyLoad();
		Map<String,String> attr = getAttributes(name);
		List<String> values = getAsList(name);
		
		// script xml defined ?
		if (attr == null) {
			return "Undefined input or option: '"+name+"'";
		}
		
		String beginText = "The "+attr.get("kind")+" '"+(attr.get("nicename") == null || "".equals(attr.get("nicename")) ? name : attr.get("nicename"))+"'";

		// is required ?
		if (values == null) {
			if ("true".equals(attr.get("required"))) {
				return beginText+" is required.";
			}
		}

		// is sequence ?
		if (!"true".equals(attr.get("sequence")) && getCount(name) != 1) {
			return beginText+" must have exactly one value, "+(getCount(name) == 0 ? "no value" : getCount(name)+" values")+" are currently given.";
		}

		String type = attr.get("type");
		if (type != null) {
			String[] typeSplit = type.split(":");
			type = typeSplit[typeSplit.length-1];

			for (String value : getAsList(name)) {
				if (value == null) {
					return beginText+" contains a undefined value.";
				}
				
				switch (attr.get("type")) {

				case "string":
					// nothing to validate, all strings are strings
					break;

				case "boolean":
					if (!"true".equals(value) && !"false".equals(value)) {
						return beginText+" must be either 'true' or 'false'.";
					}
					break;

				case "float":
					try {
						new Float(value);
					} catch (NumberFormatException e) {
						return beginText+" must be a floating point number";
					}
					break;

				case "double":
				case "decimal":
					try {
						new Double(value);
					} catch (NumberFormatException e) {
						return beginText+" must be a decimal number";
					}
					break;

				case "positiveInteger":
				case "nonNegativeInteger":
					try {
						long l = new Long(value);
						if (l < 0L) {
							return beginText+" must be a positive integer";
						}
					} catch (NumberFormatException e) {
						return beginText+" must be a positive integer";
					}
					break;

				case "nonPositiveInteger":
				case "negativeInteger":
					try {
						long l = new Long(value);
						if (l > 0L) {
							return beginText+" must be a negative integer";
						}
					} catch (NumberFormatException e) {
						return beginText+" must be a negative integer";
					}
					break;

				case "integer":
				case "int":
				case "long":
					try {
						new Long(value);
					} catch (NumberFormatException e) {
						return beginText+" must be a integer";
					}
					break;

					
				case "language":
					if (!languagePattern.matcher(value).matches()) {
						return beginText+" must be a valid language code according to RFC 3066.";
					}
					break;

				case "ID":
					if (!idPattern.matcher(value).matches()) {
						return beginText+" must be a valid XML ID.";
					}
					break;

				case "IDREF":
					if (!idrefPattern.matcher(value).matches()) {
						return beginText+" must be a valid XML IDREF.";
					}
					break;

				case "Name":
					if (!namePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML Name.";
					}
					break;

				case "NCName":
					if (!ncnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML NCName.";
					}
					break;

				case "QName":
					if (!qnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML QName.";
					}
					break;
					
				case "ENTITY":
					if (!qnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML QName.";
					}
					break;
					
				case "NMTOKEN":
					if (!qnamePattern.matcher(value).matches()) {
						return beginText+" must be a valid XML QName.";
					}
					break;
					
				case "date":
				case "time":
				case "dateTime":
				case "duration":
				case "gYearMonth":
				case "gYear":
				case "gMonthDay":
				case "gDay":
				case "gMonth":
					// TODO: add validation of time, dates, durations etc. here if needed 
					break;
					
				case "anyURI":
					try {
						URI uri = new URI(value);
						
						if ("".equals(attr.get("outputType"))) { // input type => validate that it refers to context files
							if (uri.getScheme() != null || value.startsWith("/")) {
								return beginText+" must be a relative URI";
							}
							if (directory.isDirectory()) { // only validate presence of context files if job is stored to disk
								File targetFile = new File(new File(directory, "context"), value);
								if (!targetFile.exists()) {
									return beginText+" must refer to a file or directory that exists in the job context.";
								}
							}
							
						} else { // output type => validate that the URI is an absolute file: URI
							if ("file".equals(uri.getScheme())) {
								return beginText+" must be an absolute file: URI"+(value.startsWith("/") ? "; it must start with 'file:/' and not simply '/'" : "");
							}
							File targetFile = new File(uri);
							if (!targetFile.exists()) {
								return beginText+" must refer to a file or directory that exists.";
							}
							
						}
						
					} catch (URISyntaxException e) {
						return beginText+" must be a URI.";
					}
					break;
					
				case "anyDirURI":
					try {
						URI uri = new URI(value);
						
						if ("".equals(attr.get("outputType"))) { // input type => validate that it refers to context files
							if (uri.getScheme() != null || value.startsWith("/")) {
								return beginText+" must be a relative URI";
							}
							if (directory.isDirectory()) { // only validate presence of context files if job is stored to disk
								File targetFile = new File(new File(directory, "context"), value);
								if (!targetFile.exists()) {
									return beginText+" must refer to a directory that exists in the job context.";
								}
							}
							
						} else { // output type => validate that the URI is an absolute file: URI
							if (!"file".equals(uri.getScheme())) {
								return beginText+" must be an absolute file: URI"+(value.startsWith("/") ? "; it must start with 'file:/' and not simply '/'" : "");
							}
							File targetFile = new File(uri);
							if (!targetFile.exists()) {
								return beginText+" must refer to a directory that exists.";
							}
							
						}
						
					} catch (URISyntaxException e) {
						return beginText+" must be a URI.";
					}
					break;
					
				case "anyFileURI":
					try {
						URI uri = new URI(value);
						
						if ("".equals(attr.get("outputType"))) { // input type => validate that it refers to context files
							if (uri.getScheme() != null || value.startsWith("/")) {
								return beginText+" must be a relative URI";
							}
							if (directory.isDirectory()) { // only validate presence of context files if job is stored to disk
								File targetFile = new File(new File(directory, "context"), value);
								if (!targetFile.exists()) {
									return beginText+" must refer to a file that exists in the job context.";
								}
								// NOTE: We could validate mediatype here, but let's not. It would probably not be reliable enough, and performance could be an issue.
							}
							
						} else { // output type => validate that the URI is an absolute file: URI
							if ("file".equals(uri.getScheme())) {
								return beginText+" must be an absolute file: URI"+(value.startsWith("/") ? "; it must start with 'file:/' and not simply '/'" : "");
							}
							File targetFile = new File(uri);
							if (!targetFile.exists()) {
								return beginText+" must refer to a file that exists.";
							}
							
						}
						
					} catch (URISyntaxException e) {
						return beginText+" must be a URI.";
					}
					break;
				}
			}
		}
		
		return null;
	}

	/**
	 * Check that all the inputs and options is filled out properly.
	 * 
	 * @return a message describing the error, or null if there is no error
	 */
	public String validate() {
		for (String name : optionsAndInputsAttributes.keySet()) {
			String error = validate(name);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

}
