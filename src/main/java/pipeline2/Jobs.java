package pipeline2;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pipeline2.models.script.Argument;
import pipeline2.utils.XML;


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
	 * Helper method for the two post(...) methods that creates the job request document
	 * 
	 * @param href
	 * @param options
	 * @param inputs
	 * @return
	 * @throws Pipeline2WSException 
	 */
	private static Document createJobRequestDocument(String href, List<Argument> arguments, Map<String,String> callbacks) throws Pipeline2WSException {
		Document jobRequestDocument = pipeline2.utils.XML.getXml("<jobRequest xmlns='http://www.daisy.org/ns/pipeline/data'/>");
		Element jobRequest = jobRequestDocument.getDocumentElement();

		Element element = jobRequestDocument.createElement("script");
		element.setAttribute("href", href);
		jobRequest.appendChild(element);
		
		for (Argument arg : arguments) {
			try {
				jobRequest.appendChild(arg.asDocumentElement(jobRequestDocument));
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
		
		return jobRequestDocument;
	}
	
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
		if (Pipeline2WS.debug) System.out.println(XML.toString(jobRequestDocument));
		
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
	public static Pipeline2WSResponse delete(String endpoint, String username, String secret, String id) {
		return null; //TODO Pipeline2WS.delete(endpoint, "/jobs/"+id, username, secret);
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
	public static Pipeline2WSResponse getResult(String endpoint, String username, String secret, String id) throws Pipeline2WSException {
		return Pipeline2WS.get(endpoint, "/jobs/"+id+"/result", username, secret, null);
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
		return Pipeline2WS.get(endpoint, "/jobs/"+id+"/log", username, secret, null);
	}
	
}
