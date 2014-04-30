package org.daisy.pipeline.client;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.job.JobResult;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.utils.XML;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.daisy.pipeline.utils.Files;

/**
 * Methods for communicating with the "/jobs" resource in a Pipeline 2 Web Service.
 * 
 * @see http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI#Jobs
 * 
 * @author jostein
 */
public class Jobs {
	
	/**
	 * Get all jobs
	 * 
	 * HTTP 200 OK: Response body contains XML data
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse get(String endpoint, String username, String secret) throws Pipeline2WSException {
		return Pipeline2WS.get(endpoint, "/jobs", username, secret, null);
	}
	
	/**
	 * Creates a job request document.
	 * 
	 * @param href
	 * @param options
	 * @param inputs
	 * @return
	 * @throws Pipeline2WSException 
	 */
	public static Document createJobRequestDocument(String href, List<Argument> arguments, Map<String,String> callbacks) throws Pipeline2WSException {
		Document jobRequestDocument = XML.getXml("<jobRequest xmlns='http://www.daisy.org/ns/pipeline/data'/>");
		Element jobRequest = jobRequestDocument.getDocumentElement();

		Element element = jobRequestDocument.createElement("script");
		element.setAttribute("href", href);
		jobRequest.appendChild(element);
		
		for (Argument arg : arguments) {
			try {
				Node node = arg.asDocumentElement(jobRequestDocument);
				if (node != null)
					jobRequest.appendChild(node);
			} catch (NullPointerException e) {
				throw new Pipeline2WSException("Tried to serialize generic argument", e);
			}
		}
		
		if (callbacks != null) {
			for (String callbackType : callbacks.keySet()) {
				String callbackHref = callbacks.get(callbackType);
				Element callback = jobRequestDocument.createElement("callback");
				callback.setAttribute("type", callbackType);
				callback.setAttribute("href", callbackHref);
				jobRequest.appendChild(callback);
			}
		}
		
		jobRequestDocument = XML.getXml(XML.toString(jobRequestDocument)); // fixes namespaces somehow
		
		if (Pipeline2WS.logger().logsLevel(Pipeline2WSLogger.LEVEL.DEBUG)) {
			Pipeline2WS.logger().debug("created jobRequest document:");
			Pipeline2WS.logger().debug(XML.toString(jobRequestDocument));
		}
		
		return jobRequestDocument;
	}
	
//	/**
//	 * Returns a list of all the arguments and their values in the given jobRequest document.
//	 * 
//	 * @param jobRequest
//	 * @return
//	 */
//	public static List<Argument> parseJobRequestDocumentArguments(Document jobRequest) throws Pipeline2WSException {
//		
//		List<Node> inputs = XPath.selectNodes("/*/d:input", jobRequest, Pipeline2WS.ns);
//		List<Node> outputs = XPath.selectNodes("/*/d:output", jobRequest, Pipeline2WS.ns);
//		List<Node> options = XPath.selectNodes("/*/d:option", jobRequest, Pipeline2WS.ns);
//		
//		for (Node input : inputs) {
//			
//		}
//		
		/*
		public Job(Node jobRequest) throws Pipeline2WSException {
			this();
			
			
			
			id = XPath.selectText("@id", jobRequest, Pipeline2WS.ns);
			href = XPath.selectText("@href", jobRequest, Pipeline2WS.ns);
			String status = XPath.selectText("@status", jobRequest, Pipeline2WS.ns);
			for (Status s : Status.values()) {
				if (s.toString().equals(status)) {
					this.status = s;
					break;
				}
			}
			script.id = XPath.selectText("d:script/@id", jobRequest, Pipeline2WS.ns);
			script.href = XPath.selectText("d:script/@href", jobRequest, Pipeline2WS.ns);
			script.desc = XPath.selectText("d:script/d:description", jobRequest, Pipeline2WS.ns);
			logHref = XPath.selectText("d:log/@href", jobRequest, Pipeline2WS.ns);
			resultHref = XPath.selectText("d:result/@href", jobRequest, Pipeline2WS.ns);
			
			List<Node> messageNodes = XPath.selectNodes("d:messages/d:message", jobRequest, Pipeline2WS.ns);
			for (Node messageNode : messageNodes) {
				this.messages.add(new Message(
					XPath.selectText("@level", messageNode, Pipeline2WS.ns),
					XPath.selectText("@sequence", messageNode, Pipeline2WS.ns),
					XPath.selectText(".", messageNode, Pipeline2WS.ns)
				));
			}
			Collections.sort(this.messages);
		}
		*/
//	}
	
	/**
	 * Create a job with files
	 * 
	 * HTTP 201 Created: The URI of the new job is found in the HTTP location header
	 * HTTP 400 Bad Request: Errors in the parameters such as invalid script name
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * @return 
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse post(String endpoint, String username, String secret, String href, List<Argument> arguments, File contextZipFile, Map<String,String> callbacks) throws Pipeline2WSException {
		
		Document jobRequestDocument = createJobRequestDocument(href, arguments, callbacks);
		if (Pipeline2WS.logger().logsLevel(Pipeline2WSLogger.LEVEL.DEBUG))
			Pipeline2WS.logger().debug(XML.toString(jobRequestDocument));
		
		if (contextZipFile == null) {
			return Pipeline2WS.postXml(endpoint, "/jobs", username, secret, jobRequestDocument);
			
		} else {
			File jobRequestFile = null;
			try {
				jobRequestFile = File.createTempFile("jobRequest", ".xml");
	
				StringWriter writer = new StringWriter();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(new DOMSource(jobRequestDocument), new StreamResult(writer));
				FileUtils.writeStringToFile(jobRequestFile, writer.toString());
				
			} catch (IOException e) {
				throw new Pipeline2WSException("Could not create and/or write to temporary jobRequest file", e);
			} catch (TransformerConfigurationException e) {
				throw new Pipeline2WSException("Could not serialize jobRequest XML", e);
			} catch (TransformerFactoryConfigurationError e) {
				throw new Pipeline2WSException("Could not serialize jobRequest XML", e);
			} catch (TransformerException e) {
				throw new Pipeline2WSException("Could not serialize jobRequest XML", e);
			}
			
			Map<String,File> parts = new HashMap<String,File>();
			parts.put("job-request", jobRequestFile);
			parts.put("job-data", contextZipFile);
			
			return Pipeline2WS.postMultipart(endpoint, "/jobs", username, secret, parts);
		}
		
	}
	
	/**
	 * Get a single job
	 * 
	 * HTTP 200 OK: Response body contains XML data
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * HTTP 404 Not Found: Resource not found
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse get(String endpoint, String username, String secret, String id, Integer fromSequence) throws Pipeline2WSException {
		if (fromSequence == null) {
			return Pipeline2WS.get(endpoint, "/jobs/"+id, username, secret, null);
		} else {
			Map<String,String> parameters = new HashMap<String,String>();
			parameters.put("msgSeq", fromSequence+"");
			return Pipeline2WS.get(endpoint, "/jobs/"+id, username, secret, parameters);
		}
	}
	
	/**
	 * Delete a single job
	 * 
	 * HTTP 204 No Content: Successfully processed the request, no content being returned
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * HTTP 404 Not Found: Resource not found
	 */
	public static Pipeline2WSResponse delete(String endpoint, String username, String secret, String id) throws Pipeline2WSException {
		return Pipeline2WS.delete(endpoint, "/jobs/"+id, username, secret, null);
	}
	
	/**
	 * Get the result for a job
	 * 
	 * HTTP 200 OK: Response body contains Zip data
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * HTTP 404 Not Found: Resource not found
	 * @return 
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse getResult(String endpoint, String username, String secret, String id, String href) throws Pipeline2WSException {
		if (href == null || "".equals(href))
			href = "";
		else
			href = "/"+href.replace(" ", "%20");
		return Pipeline2WS.get(endpoint, "/jobs/"+id+"/result"+href, username, secret, null);
	}
	
	/**
	 * Get the result for a job directly from disk (only works when fsallow=true)
	 * 
	 * @return The File if you are authorized to read the file and the file exists. Otherwise, null.
	 * @throws Pipeline2WSException 
	 */
	public static File getResultFromFile(String endpoint, String username, String secret, String id, String href) throws Pipeline2WSException {
		if (href == null) href = "";
		Pipeline2WSResponse jobResponse = get(endpoint, username, secret, id, null);
		Job job = new Job(jobResponse);
		Pipeline2WS.logger().debug("job.getResultByHref(\""+href+"\",\""+job.href+"/result"+"\")");
		JobResult result = job.getResultByHref(href, job.href+"/result");
		if (result == null) {
			throw new Pipeline2WSException("Could not find href in job results.");
		}
		
		File resultFile;
		
		if (result.file != null && result.file.length() > 0) {
			// single file download
			try {
				Pipeline2WS.logger().debug("Reading file from disk: \""+result.file+"\" (href: \""+href+"\")");
				resultFile = new File(new URI(result.file));
			} catch (URISyntaxException e) {
				throw new Pipeline2WSException("Unable to parse result file path; please make sure that the Pipeline 2 engine is running on the same system as the the client (i.e. the Web UI).", e);
			} catch (IllegalArgumentException e) {
				throw new Pipeline2WSException("Could not read file from disk: "+result.file, e);
			}
		}
		
		else if (result.from != null && result.from.length() > 0) {
			// option or port
			try {
				File tempDirForZip = File.createTempFile("webui-result-zip", null);
				Pipeline2WS.logger().debug("creating temp dir for zip: "+tempDirForZip.getAbsolutePath());
				tempDirForZip.delete();
				tempDirForZip.mkdir();
				
				resultFile = new File(new URI(tempDirForZip.toURI().toString()+"/"+id+"-"+result.name+".zip"));
				Pipeline2WS.logger().debug("touching zip: "+resultFile.getAbsolutePath());
				resultFile.createNewFile();
				
				JobResult firstChild = result.results.get(0);
				if (firstChild != null) {
					String directoryPath = firstChild.file.substring(0, firstChild.file.length()-firstChild.filename.length());
					Files.addDirectoryToZip(resultFile, new File(new URI(directoryPath)));
				}
				
			} catch (IOException e) {
				throw new Pipeline2WSException("Unable to create result ZIP archive for "+result.from+" "+result.name, e);
			} catch (URISyntaxException e) {
				throw new Pipeline2WSException("Unable to create result ZIP archive for "+result.from+" "+result.name, e);
			}
		}
		
		else {
			// entire result
			try {
				File tempDirForZip = File.createTempFile("webui-result-zip", null);
				Pipeline2WS.logger().debug("creating temp dir for zip: "+tempDirForZip.getAbsolutePath());
				tempDirForZip.delete();
				tempDirForZip.mkdir();
				
				resultFile = new File(new URI(tempDirForZip.toURI().toString()+"/"+id+"-"+result.name+".zip"));
				Pipeline2WS.logger().debug("touching zip: "+resultFile.getAbsolutePath());
				resultFile.createNewFile();
				
				boolean optionAndPortWithSameName = false;
				List<String> optionAndPortNames = new ArrayList<String>();
				for (JobResult optionOrPort : result.results) {
					if (optionAndPortNames.contains(optionOrPort.name)) {
						optionAndPortWithSameName = true;
						break;
					} else {
						optionAndPortNames.add(optionOrPort.name);
					}
				}
				
				for (JobResult optionOrPort : result.results) {
					JobResult firstChild = optionOrPort.results.get(0);
					if (firstChild != null) {
						String directoryPath = firstChild.file.substring(0, firstChild.file.length()-firstChild.filename.length());
						File directory = new File(new URI(directoryPath));
						Map<String, File> files = Files.listFilesRecursively(directory, directory.getParentFile().toURI(), true);
						Map<String, File> filesInDir = new HashMap<String, File>();
						if (optionAndPortWithSameName) {
							for (String fileHref : files.keySet()) {
								filesInDir.put(optionOrPort.from+"/"+fileHref, files.get(fileHref));
							}
						} else {
							filesInDir = files;
						}
						Files.addFilesToZip(resultFile, filesInDir);
					}
				}
				
			} catch (IOException e) {
				throw new Pipeline2WSException("Unable to create result ZIP archive for "+result.from+" "+result.name, e);
			} catch (URISyntaxException e) {
				throw new Pipeline2WSException("Unable to create result ZIP archive for "+result.from+" "+result.name, e);
			}
		}
		
		return resultFile;
	}
	
	/**
	 * Get the log file for a job
	 * 
	 * HTTP 200 OK: Response body contains plain text data
	 * HTTP 401 Unauthorized: Client was not authorized to perform request.
	 * HTTP 404 Not Found: Resource not found
	 * @throws Pipeline2WSException 
	 */
	public static Pipeline2WSResponse getLog(String endpoint, String username, String secret, String id) throws Pipeline2WSException {
		Pipeline2WS.logger().debug("getting log...");
		return Pipeline2WS.get(endpoint, "/jobs/"+id+"/log", username, secret, null);
	}
	
}
