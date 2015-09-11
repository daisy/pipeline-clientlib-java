package org.daisy.pipeline.client.models;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.filestorage.JobStorageInterface;
import org.daisy.pipeline.client.filestorage.JobValidator;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	private String scriptHref; // for job request xml documents
	private Script script;
	private String nicename;
	private String batchId;
	private List<Argument> argumentInputs; // used when there's no script given
	private List<Argument> argumentOutputs; // used when there's no script given
	private List<Callback> callback;
	private List<Message> messages;
	private Node messagesNode;
	private String logHref;
	private Result result; // "all results"-zip
	private SortedMap<Result,List<Result>> results; // "individual results"-zips as keys, individual files as values
	private Node resultsNode;

	private boolean lazyLoaded = false;
	private Node jobNode = null;
	
	private JobStorageInterface context;
	
	private static final Map<String,String> ns;
	static {
		ns = new HashMap<String,String>();
		ns.put("d", "http://www.daisy.org/ns/pipeline/data");
	}
	
	/** Create an empty representation of a job. */
	public Job() {}
	
	/**
	 * Parse the job described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/job.xml
	 * 
	 * @param jobXml
	 * @throws Pipeline2Exception
	 */
	public Job(Node jobXml) throws Pipeline2Exception {
		this();
		setJobXml(jobXml);
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
	public static Result getResultFromHref(Node jobXml, String href) {
		if (href == null) {
			href = "";
		}
		
		try {
			if (jobXml instanceof Document) {
				jobXml = XPath.selectNode("/d:job", jobXml, XPath.dp2ns);
			}
			
			String base = XPath.selectText("@href", jobXml, XPath.dp2ns) + "/result";
			if (!"".equals(href)) {
				base += "/"; 
			}
			String fullHref = base + href;
			
			Node resultNode = XPath.selectNode("d:results//descendant-or-self::*[@href='"+fullHref.replaceAll("'", "''").replaceAll(" ", "%20")+"']", jobXml, XPath.dp2ns);
			if (resultNode != null) {
				String parentHref = XPath.selectText("../@href", resultNode, XPath.dp2ns); // can be from /job/@href, /job/results/@href or /job/results/result/@href
				if (parentHref != null) {
					base = parentHref;
				}
				return Result.parseResultXml(resultNode, base);
			}
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Could not parse job XML for finding the job result '"+href+"'", e);
		}
		return null;
	}
	
	private void lazyLoad() {
		if (context != null) {
			context.lazyLoad();
		}
		
		if (lazyLoaded || jobNode == null) {
			return;
		}
		
		try {
			// select root element if the node is a document node
			if (jobNode instanceof Document)
				jobNode = XPath.selectNode("/*", jobNode, XPath.dp2ns);
			
			String id = XPath.selectText("@id", jobNode, XPath.dp2ns);
			if (id != null) {
				this.id = id;
			}
			this.href = XPath.selectText("@href", jobNode, XPath.dp2ns);
			String status = XPath.selectText("@status", jobNode, XPath.dp2ns);
			for (Status s : Status.values()) {
				if (s.toString().equals(status)) {
					this.status = s;
					break;
				}
			}
			String priority = XPath.selectText("@priority", jobNode, XPath.dp2ns);
			for (Priority p : Priority.values()) {
				if (p.toString().equals(priority)) {
					this.priority = p;
					break;
				}
			}
			if (this.priority == null) {
				// in the job XML, the priority is a attribute, but in the job request xml, it is an element
				priority = XPath.selectText("priority", jobNode, XPath.dp2ns);
				for (Priority p : Priority.values()) {
					if (p.toString().equals(priority)) {
						this.priority = p;
						break;
					}
				}
			}

			if (XPath.selectNode("d:script/*", jobNode, XPath.dp2ns) == null) {
				scriptHref = XPath.selectText("d:script/@href", jobNode, XPath.dp2ns);
				
			} else {
				Node scriptNode = XPath.selectNode("d:script", jobNode, XPath.dp2ns);
				if (scriptNode != null) {
					this.script = new Script(scriptNode);
				}
			}
			this.nicename = XPath.selectText("d:nicename", jobNode, XPath.dp2ns);
			this.batchId = XPath.selectText("d:batchId", jobNode, XPath.dp2ns);
			this.logHref = XPath.selectText("d:log/@href", jobNode, XPath.dp2ns);
			this.callback = new ArrayList<Callback>();
			for (Node callbackNode : XPath.selectNodes("d:callback", jobNode, XPath.dp2ns)) {
				this.callback.add(new Callback(callbackNode));
			}
			this.messagesNode = XPath.selectNode("d:messages", jobNode, XPath.dp2ns);
			this.resultsNode = XPath.selectNode("d:results", jobNode, XPath.dp2ns);
			
			// Arguments are both part of the script XML and the jobRequest XML.
			// We could keep a separate copy of the arguments here in the Job instance, but that seems a bit unneccessary.
			// We could keep the argument values here in the Job instance and the argument definitions in the Script instance,
			// but that will probably just complicate validation and separates things that are closely related.
			// Instead, we just say that everything is stored in the Script instance (if there is one).
			// However, if there is no script defined, we keep the arguments her in the job instance.
			if (this.script == null) {
				argumentInputs = new ArrayList<Argument>();
				argumentOutputs = new ArrayList<Argument>();
				for (Node node : XPath.selectNodes("d:input | d:option", jobNode, XPath.dp2ns)) {
					argumentInputs.add(new Argument(node));
				}
				for (Node node : XPath.selectNodes("d:output", jobNode, XPath.dp2ns)) {
					argumentOutputs.add(new Argument(node));
				}
			}
			

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse job XML", e);
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
				
				messages = new ArrayList<Message>();
				List<Node> messageNodes = XPath.selectNodes("d:message", this.messagesNode, XPath.dp2ns);
				
				for (Node messageNode : messageNodes) {
					Message m = new Message();
					m.text = XPath.selectText(".", messageNode, XPath.dp2ns);
					if (XPath.selectText("@level", messageNode, XPath.dp2ns) != null) {
					    m.level = Message.Level.valueOf(XPath.selectText("@level", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@sequence", messageNode, XPath.dp2ns) != null) {
					    m.sequence = Integer.valueOf(XPath.selectText("@sequence", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@line", messageNode, XPath.dp2ns) != null) {
					    m.line = Integer.valueOf(XPath.selectText("@line", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@column", messageNode, XPath.dp2ns) != null) {
					    m.column = Integer.valueOf(XPath.selectText("@column", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@timeStamp", messageNode, XPath.dp2ns) != null) {
					    m.timeStamp = XPath.selectText("@timeStamp", messageNode, XPath.dp2ns);
					}
					if (XPath.selectText("@file", messageNode, XPath.dp2ns) != null) {
					    m.file = XPath.selectText("@file", messageNode, XPath.dp2ns);
					}
					messages.add(m);
				}
				Collections.sort(messages);
			
			} catch (Exception e) {
				Pipeline2Logger.logger().error("Unable to parse messages XML", e);
			}
		}
		
		return messages;
	}
	
	private void lazyLoadResults() {
		lazyLoad();
		if (results == null && resultsNode != null) {
			try {
				result = Result.parseResultXml(this.resultsNode, href);
				results = new TreeMap<Result,List<Result>>();
				
				List<Node> resultNodes = XPath.selectNodes("d:result", this.resultsNode, XPath.dp2ns);
				for (Node resultPortOrOptionNode : resultNodes) {
					Result resultPortOrOption = Result.parseResultXml(resultPortOrOptionNode, result.href);
					List<Result> portOrOptionResults = new ArrayList<Result>();
					
					List<Node> fileNodes = XPath.selectNodes("d:result", resultPortOrOptionNode, XPath.dp2ns);
					for (Node fileNode : fileNodes) {
						Result file = Result.parseResultXml(fileNode, resultPortOrOption.href);
						portOrOptionResults.add(file);
					}
					Collections.sort(portOrOptionResults);
					
					results.put(resultPortOrOption, portOrOptionResults);
				}
				
			} catch (Pipeline2Exception e) {
				Pipeline2Logger.logger().error("Unable to parse results XML", e);
			}
		}
	}
	
	/**
	 * Get the main Result object.
	 * 
	 * @return The returned Result represents "all results".
	 */
	public Result getResult() {
		lazyLoadResults();
		return result;
	}
	
	/**
	 * Get the Result representing the argument with the given name.
	 * 
	 * @param argumentName
	 * @return
	 */
	public Result getResult(String argumentName) {
		lazyLoadResults();
		if (argumentName == null) {
			return null;
		}
		for (Result result : getResults().keySet()) {
			if (argumentName.equals(result.name)) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Get the Result representing the file with the given name from the argument with the given name.
	 * 
	 * @param argumentName
	 * @param href
	 * @return
	 */
	public Result getResult(String argumentName, String href) {
		lazyLoadResults();
		if (argumentName == null || href == null || results == null) {
			return null;
		}
		
		href = href.replaceAll(" ", "%20");
		
		Result argument = getResult(argumentName);
		if (!results.containsKey(argument)) {
			return null;
		}
		
		for (Result r : getResults(argumentName)) {
			if (href.equals(r.href) || href.equals(r.relativeHref)) {
				return r;
			}
		}
		
		return null;
	}
	
	/**
	 * Get the list of Results for the argument with the given name.
	 * 
	 * @param argumentName
	 * @return
	 */
	public List<Result> getResults(String argumentName) {
		if (argumentName == null) {
			return null;
		}
		for (Result result : getResults().keySet()) {
			if (argumentName.equals(result.name)) {
				return results.get(result);
			}
		}
		return null;
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
	public SortedMap<Result,List<Result>> getResults() {
		lazyLoad();
		lazyLoadResults();
		return results;
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
			jobsXml = XPath.selectNode("/d:jobs", jobsXml, XPath.dp2ns);
		
		List<Node> jobNodes = XPath.selectNodes("d:job", jobsXml, XPath.dp2ns);
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
		for (Argument argument : getInputs()) {
			String error = JobValidator.validate(argument, context);
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
		for (Argument argument : getInputs()) {
			if (argument.getName().equals(name)) {
				return JobValidator.validate(argument, context);
			}
		}
		return null;
	}
	
	// simple getters and setters (to ensure lazy loading is performed)
	public String getId() { lazyLoad(); return id; }
	public String getHref() { lazyLoad(); return href; }
	public String getScriptHref() { lazyLoad(); if (script != null) return script.getHref(); else return scriptHref; }
	public Status getStatus() { lazyLoad(); return status; }
	public String getLogHref() { lazyLoad(); return logHref; }
	public String getNicename() { lazyLoad(); return nicename; }
	public String getBatchId() { lazyLoad(); return batchId; }
	public Priority getPriority() { lazyLoad(); return priority; }
	public List<Callback> getCallback() { lazyLoad(); return callback; }
	public JobStorageInterface getContext() { lazyLoad(); return context; }
	public void setId(String id) { lazyLoad(); this.id = id; }
	public void setNicename(String nicename) { lazyLoad(); this.nicename = nicename; }
	public void setBatchId(String batchId) { lazyLoad(); this.batchId = batchId; }
	public void setPriority(Priority priority) { lazyLoad(); this.priority = priority; }
	public void setCallback(List<Callback> callback) { lazyLoad(); this.callback = callback; }
	public void setContext(JobStorageInterface context) { lazyLoad(); this.context = context; }
	
	/** Set job XML and re-enable lazy loading for the new XML. */
	public void setJobXml(Node jobXml) {
		this.jobNode = jobXml;
		this.lazyLoaded = false;
	}
	
	public Script getScript() {
		lazyLoad();
		return script;
	}
	
	public void setScript(Script script) {
		lazyLoad();
		this.script = script;
	}
	
	public List<Argument> getInputs() {
		lazyLoad();
		return script == null ? argumentInputs : script.getInputs();
	}
	
	public List<Argument> getOutputs() {
		lazyLoad();
		return script == null ? argumentOutputs : script.getOutputs();
	}
	
	public Argument getArgument(String name) {
		lazyLoad();
		for (Argument arg : getInputs()) {
			if (arg.getName().equals(name)) {
				return arg;
			}
		}
		for (Argument arg : getOutputs()) {
			if (arg.getName().equals(name)) {
				return arg;
			}
		}
		return null;
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
	
	public Result getResultFromHref(String href) {
		lazyLoad();
		if (href == null) {
			return null;
		}
		if (href.equals(result.relativeHref)) {
			return result;
		}
		for (Result key : results.keySet()) {
			if (href.equals(key.relativeHref)) {
				return key;
			}
			for (Result value : results.get(key)) {
				if (href.equals(value.relativeHref)) {
					return key;
				}
			}
		}
		return null;
	}

	public Document toXml() {
		lazyLoad();
		
		Document jobDocument = XML.getXml("<d:job xmlns:d=\"http://www.daisy.org/ns/pipeline/data\"/>");
		Element jobElement = jobDocument.getDocumentElement();

		if (id != null) {
			jobElement.setAttribute("id", id);
		}
		
		if (href != null) {
			jobElement.setAttribute("href", href);
		}
		
		if (status != null) {
			jobElement.setAttribute("status", status.toString());
		}
		
		if (priority != null) {
			jobElement.setAttribute("priority", priority.toString());
		}
		
		if (script != null) {
			XML.appendChildAcrossDocuments(jobElement, script.toXml().getDocumentElement());
		}
		
		if (nicename != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:nicename");
			e.setTextContent(nicename);
			jobElement.appendChild(e);
		}
		
		if (batchId != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:batchId");
			e.setTextContent(batchId);
			jobElement.appendChild(e);
		}
		
		if (logHref != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:log");
			e.setAttribute("href", logHref);
			jobElement.appendChild(e);
		}
		
		if (callback != null) {
			for (Callback c : callback) {
				XML.appendChildAcrossDocuments(jobElement, c.toXml().getDocumentElement());
			}
		}
		
		if (messages != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:messages");
			for (Message m : messages) {
				Element mElement = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:message");
				if (m.level != null) {
					mElement.setAttribute("level", m.level.toString());
				}
				if (m.sequence != null) {
					mElement.setAttribute("sequence", ""+m.sequence);
				}
				if (m.line != null) {
					mElement.setAttribute("line", ""+m.sequence);
				}
				if (m.column != null) {
					mElement.setAttribute("column", ""+m.sequence);
				}
				if (m.timeStamp != null) {
					mElement.setAttribute("timeStamp", m.timeStamp);
				}
				if (m.file != null) {
					mElement.setAttribute("file", m.file);
				}
			    if (m.text != null) {
			    	mElement.setTextContent(m.text);
			    }
			}
			jobElement.appendChild(e);
		}
		
		if (results != null) {
			Element resultsElement = jobDocument.createElementNS(XPath.dp2ns.get("d"), "d:results");
			result.toXml(resultsElement);
			for (Result r : results.keySet()) {
				Element resultElement = jobDocument.createElementNS(XPath.dp2ns.get("d"), "d:result");
				r.toXml(resultElement);
				for (Result fileResult : results.get(r)) {
					Element fileElement = jobDocument.createElementNS(XPath.dp2ns.get("d"), "d:result");
					fileResult.toXml(fileElement);
				}
				resultsElement.appendChild(resultElement);
			}
			jobElement.appendChild(resultsElement);
		}
		
		// input and option values are stored in /job/script/* instead of here; no need to mirror those values here if the script is defined
		if (script == null) {
			if (argumentInputs != null) {
				for (Argument arg : argumentInputs) {
					XML.appendChildAcrossDocuments(jobElement, arg.toXml().getDocumentElement());
				}
			}
			if (argumentOutputs != null) {
				for (Argument arg : argumentOutputs) {
					XML.appendChildAcrossDocuments(jobElement, arg.toXml().getDocumentElement());
				}
			}
		}
		
		return jobDocument;
	}

	public Document toJobRequestXml() {
		lazyLoad();
		
		Document jobRequestDocument = XML.getXml("<d:jobRequest xmlns:d=\"http://www.daisy.org/ns/pipeline/data\"/>");
		Element jobRequestElement = jobRequestDocument.getDocumentElement();

		if (script != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:script");
			e.setAttribute("href", script.getHref());
			jobRequestElement.appendChild(e);
			
		} else if (scriptHref != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:script");
			e.setAttribute("href", scriptHref);
			jobRequestElement.appendChild(e);
		}
		
		if (nicename != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:nicename");
			e.setTextContent(nicename);
			jobRequestElement.appendChild(e);
		}
		
		if (priority != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:priority");
			e.setTextContent(priority.toString());
			jobRequestElement.appendChild(e);
		}
		
		if (batchId != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:batchId");
			e.setTextContent(batchId);
			jobRequestElement.appendChild(e);
		}
		
		List<Argument> options = new ArrayList<Argument>();
		List<Argument> inputs = new ArrayList<Argument>();
		List<Argument> outputs = getOutputs();
		for (Argument inputOrOption : getInputs()) {
			if (inputOrOption.getKind() == Argument.Kind.input) {
				inputs.add(inputOrOption);
			} else {
				options.add(inputOrOption);
			}
		}
		for (Argument input : inputs) {
			if (input.isDefined()) {
				Element arg = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:input");
				arg.setAttribute("name", input.getName());

				for (String value : input.getAsList()) {
					Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:item");
					item.setAttribute("value", value);
					arg.appendChild(item);
				}
				jobRequestElement.appendChild(arg);
			}
		}
		for (Argument option : options) {
			if (option.isDefined()) {
				Element arg = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:option");
				arg.setAttribute("name", option.getName());
				if (option.getSequence()) {
					for (String value : option.getAsList()) {
						Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:item");
						item.setAttribute("value", value);
						arg.appendChild(item);
					}
				} else {
					arg.setTextContent(option.get());
				}
				jobRequestElement.appendChild(arg);
			}
		}
		for (Argument output : outputs) {
			if (output.isDefined()) {
				Element arg = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:output");
				arg.setAttribute("name", output.getName());
				for (String value : output.getAsList()) {
					Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "d:item");
					item.setAttribute("value", value);
					arg.appendChild(item);
				}
				jobRequestElement.appendChild(arg);
			}
		}
		
		if (callback != null) {
			for (Callback c : callback) {
				jobRequestElement.appendChild(c.toXml());
			}
		}
		
		return jobRequestDocument;
	}

	/**
	 * Get all arguments, both inputs and outputs.
	 * 
	 * See also getInputs and getOutputs.
	 * 
	 * @return a list of all arguments.
	 */
	public List<Argument> getArguments() {
		List<Argument> arguments = new ArrayList<Argument>();
		arguments.addAll(getInputs());
		arguments.addAll(getOutputs());
		return arguments;
	}

}
