package org.daisy.pipeline.client.models;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.daisy.pipeline.client.Pipeline2Client;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.models.job.Callback;
import org.daisy.pipeline.client.models.job.Result;
import org.daisy.pipeline.client.models.job.Message;
import org.daisy.pipeline.client.models.script.Argument;
import org.daisy.pipeline.client.models.script.ArgumentValidator;
import org.daisy.pipeline.client.persistence.Context;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of a Pipeline 2 job.
 * 
 * This represents a job both before (job request) and after (job response) it is submitted to the engine.
 * 
 * @author jostein
 */
public class Job implements Comparable<Job> {
	
	public enum Status { IDLE, RUNNING, DONE, ERROR, VALIDATION_FAIL };
	public enum Priority { high, medium, low };
	
	private String id;
	private String href; // xs:anyURI
	private Status status;
	private Priority priority;
	private Script script;
	private Node scriptNode;
	private String niceName;
	private String batchId; // TODO: methods etc. Should equal the /*/batchId/text() from the job xml
	private List<Node> argumentsNodes;
	private List<Callback> callback;
	private List<Message> messages;
	private Node messagesNode;
	private String logHref;
	private Result result; // "all results"-zip
	private Map<Result,List<Result>> results; // "individual results"-zips as keys, individual files as values
	private Node resultsNode;

	private boolean lazyLoaded = false;
	private Node jobNode = null;
	
	private Context context;
	
	private static final Map<String,String> ns;
	static {
		ns = new HashMap<String,String>();
		ns.put("d", "http://www.daisy.org/ns/pipeline/data");
	}
	
	/**
	 * Create an empty representation of a job.
	 */
	public Job() {
		//script = new Script();
	}
	
	/**
	 * Parse the job described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @throws Pipeline2Exception
	 */
	public Job(Pipeline2WSResponse response) throws Pipeline2Exception {
		this();
		if (response.status != 200)
			throw new Pipeline2Exception(response.status+" "+response.statusName+": "+response.statusDescription);
		jobNode = response.asXml();
	}
	
	/**
	 * Parse the job described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/job.xml
	 * 
	 * @param jobXml
	 * @throws Pipeline2Exception
	 */
	public Job(Node jobXml) throws Pipeline2Exception {
		this();
		jobNode = jobXml;
	}
	
	/**
	 * Get the job result matching the given href without parsing the entire jobXml.
	 * 
	 * @param jobXml
	 * @param href
	 * @param base
	 * @return
	 * @throws Pipeline2Exception
	 * @throws URISyntaxException
	 */
	public static Result getResultFromHref(Node jobXml, String href) throws Pipeline2Exception {
		if (href == null) {
			href = "";
		}
		
		if (jobXml instanceof Document) {
			jobXml = XPath.selectNode("/d:job", jobXml, Pipeline2Client.ns);
		}
		
		String base = XPath.selectText("@href", jobXml, Pipeline2Client.ns) + "/result";
		if (!"".equals(href)) {
			base += "/"; 
		}
		String fullHref = base + href;
		
		Node resultNode = XPath.selectNode("d:results//descendant-or-self::*[@href='"+fullHref.replaceAll("'", "''").replaceAll(" ", "%20")+"']", jobXml, Pipeline2Client.ns);
		if (resultNode != null) {
			return Result.parseResultXml(resultNode, base);
		}
		return null;
	}
	
	private void lazyLoad() {
		if (lazyLoaded || jobNode == null) {
			return;
		}
		
		try {
			// select root element if the node is a document node
			if (jobNode instanceof Document)
				jobNode = XPath.selectNode("/*", jobNode, Pipeline2Client.ns);
			
			this.id = XPath.selectText("@id", jobNode, Pipeline2Client.ns);
			this.href = XPath.selectText("@href", jobNode, Pipeline2Client.ns);
			String status = XPath.selectText("@status", jobNode, Pipeline2Client.ns);
			for (Status s : Status.values()) {
				if (s.toString().equals(status)) {
					this.status = s;
					break;
				}
			}
			String priority = XPath.selectText("@priority", jobNode, Pipeline2Client.ns);
			for (Priority p : Priority.values()) {
				if (p.toString().equals(priority)) {
					this.priority = p;
					break;
				}
			}
			this.scriptNode = XPath.selectNode("d:script", jobNode, Pipeline2Client.ns);
			this.niceName = XPath.selectText("d:nicename", jobNode, Pipeline2Client.ns);
			this.logHref = XPath.selectText("d:log/@href", jobNode, Pipeline2Client.ns);
			this.callback = new ArrayList<Callback>();
			for (Node callbackNode : XPath.selectNodes("d:callback", jobNode, Pipeline2Client.ns)) {
				this.callback.add(new Callback(callbackNode));
			}
			this.messagesNode = XPath.selectNode("d:messages", jobNode, Pipeline2Client.ns);
			this.resultsNode = XPath.selectNode("d:results", jobNode, Pipeline2Client.ns);
			
			// Arguments are both part of the script XML and the jobRequest XML.
			// We could keep a separate copy of the arguments here in the Job instance, but that seems a bit unneccessary.
			// We could keep the argument values here in the Job instance and the argument definitions in the Script instance,
			// but that will probably just complicate validation and separates things that are closely related.
			// Instead, we just say that everything is stored in the Script instance (if there is one).
			this.argumentsNodes = XPath.selectNodes("d:input | d:option | d:output", jobNode, Pipeline2Client.ns);
			

		} catch (Pipeline2Exception e) {
			Pipeline2Client.logger().error("Unable to parse job XML", e);
		}

		lazyLoaded = true;
	}
	
	/**
	 * Get a list of all messages for the job.
	 * @return
	 * @throws Pipeline2Exception
	 */
	public List<Message> getMessages() throws Pipeline2Exception {
		lazyLoad();
		
		if (messages == null && messagesNode != null) {
			try {
				
				List<Message> messages = new ArrayList<Message>();
				List<Node> messageNodes = XPath.selectNodes("d:message", this.messagesNode, Pipeline2Client.ns);
				
				for (Node messageNode : messageNodes) {
					messages.add(new Message(
						XPath.selectText("@level", messageNode, Pipeline2Client.ns),
						XPath.selectText("@sequence", messageNode, Pipeline2Client.ns),
						XPath.selectText(".", messageNode, Pipeline2Client.ns)
					));
				}
				Collections.sort(messages);
			
			} catch (Pipeline2Exception e) {
				Pipeline2Client.logger().error("Unable to parse messages XML", e);
			}
		}
		
		return messages;
	}
	
	private void lazyLoadResults() {
		if (results == null && resultsNode != null) {
			try {
				Result outputs = Result.parseResultXml(this.resultsNode, href);
				outputs.results = new ArrayList<Result>();
				
				List<Node> outputNodes = XPath.selectNodes("d:result", this.resultsNode, Pipeline2Client.ns);
				for (Node outputNode : outputNodes) {
					Result output = Result.parseResultXml(outputNode, href);
					output.results = new ArrayList<Result>();
					
					List<Node> fileNodes = XPath.selectNodes("d:result", outputNode, Pipeline2Client.ns);
					for (Node fileNode : fileNodes) {
						Result file = Result.parseResultXml(fileNode, href);
						output.results.add(file);
					}
					Collections.sort(output.results);
					
					outputs.results.add(output);
				}
				
			} catch (Pipeline2Exception e) {
				Pipeline2Client.logger().error("Unable to parse results XML", e);
			}
		}
	}
	
	/**
	 * Get the main Result object.
	 * 
	 * @return The returned Result represents "all results".
	 */
	public Result getResult() {
		lazyLoad();
		lazyLoadResults();
		return result;
	}
	
	/**
	 * Get a map of all the Result objects.
	 * 
	 * The keys are the result objects for each named output.
	 * 
	 * Each key is associated with a list of Result objects,
	 * each representing a file in the named output.
	 * 
	 * @return The returned map contains all named outputs and associated files.
	 */
	public Map<Result,List<Result>> getResults() {
		lazyLoad();
		lazyLoadResults();
		return results;
	}
	
	/**
	 * Parse the list of jobs described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2Exception
	 */
	public static List<Job> getJobs(Pipeline2WSResponse response) throws Pipeline2Exception {
		if (response.status != 200)
			throw new Pipeline2Exception(response.status+" "+response.statusName+": "+response.statusDescription);
		return parseJobsXml(response.asXml());
	}
	
	/**
	 * Parse the list of jobs described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/jobs.xml
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2Exception
	 */
	public static List<Job> parseJobsXml(Node jobsXml) throws Pipeline2Exception {
		List<Job> jobs = new ArrayList<Job>();
		
		// select root element if the node is a document node
		if (jobsXml instanceof Document)
			jobsXml = XPath.selectNode("/d:jobs", jobsXml, Pipeline2Client.ns);
		
		List<Node> jobNodes = XPath.selectNodes("d:job", jobsXml, Pipeline2Client.ns);
		for (Node jobNode : jobNodes) {
			jobs.add(new Job(jobNode));
		}
		
		return jobs;
	}
	
	/**
	 * Check that all the inputs and options is filled out properly.
	 * 
	 * The script associated with this job must be defined.
	 * 
	 * @return a message describing the first error, or null if there is no error
	 */
	public String validate() {
		for (Argument argument : getArguments()) {
			String error = ArgumentValidator.validate(argument, context);
			if (error != null) {
				return error;
			}
		}
		return null;
	}
	
	/**
	 * Check that the argument with then provided name is filled out properly.
	 * 
	 * The script associated with this job must be defined.
	 * 
	 * @return a message describing the error, or null if there is no error
	 */
	public String validate(String name) {
		for (Argument argument : getArguments()) {
			if (argument.name.equals(name)) {
				return ArgumentValidator.validate(argument, context);
			}
		}
		return null;
	}
	
	// simple getters and setters (to ensure lazy loading is performed)
	public String getId() { lazyLoad(); return id; }
	public String getHref() { lazyLoad(); return href; }
	public Status getStatus() { lazyLoad(); return status; }
	public String getLogHref() { lazyLoad(); return logHref; }
	public String getNicename() { lazyLoad(); return niceName; }
	public Priority getPriority() { lazyLoad(); return priority; }
	public List<Callback> getCallback() { lazyLoad(); return callback; }
	public Context getContext() { lazyLoad(); return context; }
	public void setNicename(String nicename) { lazyLoad(); this.niceName = nicename; }
	public void setPriority(Priority priority) { lazyLoad(); this.priority = priority; }
	public void setCallback(List<Callback> callback) { lazyLoad(); this.callback = callback; }
	public void setContext(Context context) { lazyLoad(); this.context = context; }
	
	private void lazyLoadScriptAndArguments() {
		if (script == null && scriptNode != null) {
			try {
				script = new Script(scriptNode);
				
				if (argumentsNodes != null) {
					// TODO: lazy load arguments
				}
				
			} catch (Pipeline2Exception e) {
				Pipeline2Client.logger().error("Unable to parse script XML", e);
			}
		}
	}
	
	public Script getScript() {
		lazyLoad();
		lazyLoadScriptAndArguments();
		return script;
	}
	
	public List<Argument> getArguments() {
		lazyLoad();
		lazyLoadScriptAndArguments();
		return script == null ? null : script.getInputs();
	}
	
	@Override
	public int compareTo(Job o) {
		if (id == null && o.id == null)
			return 0;
		
		if (id == null && o.id != null)
			return -1;
		
		if (id != null && o.id == null)
			return 1;
		
		return id.compareTo(o.id);
	}
}
