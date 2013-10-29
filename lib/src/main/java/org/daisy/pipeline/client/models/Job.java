package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2WS;
import org.daisy.pipeline.client.Pipeline2WSException;
import org.daisy.pipeline.client.Pipeline2WSResponse;
import org.daisy.pipeline.client.models.job.JobResult;
import org.daisy.pipeline.client.models.job.Message;
import org.daisy.pipeline.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of a Pipeline 2 job.
 * 
 * @author jostein
 */
public class Job {
	
	public enum Status { IDLE, RUNNING, DONE, ERROR, VALIDATION_FAIL };
	
	public String id;
	public String href; // xs:anyURI
	public Status status;
	
	public Script script;
	public List<Message> messages;
	public String logHref;
	public JobResult results;
	
	/**
	 * Create an empty representation of a job.
	 */
	public Job() {
		script = new Script();
		messages = new ArrayList<Message>();
	}
	
	/**
	 * Parse the job described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @throws Pipeline2WSException
	 */
	public Job(Pipeline2WSResponse response) throws Pipeline2WSException {
		this();
		if (response.status != 200)
			throw new Pipeline2WSException(response.status+" "+response.statusName+": "+response.statusDescription);
		parseJobXml(response.asXml());
	}
	
	/**
	 * Parse the job described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/job.xml
	 * 
	 * @param jobXml
	 * @throws Pipeline2WSException
	 */
	public Job(Node jobXml) throws Pipeline2WSException {
		this();
		parseJobXml(jobXml);
	}
	
	private void parseJobXml(Node jobXml) throws Pipeline2WSException {
		// select root element if the node is a document node
		if (jobXml instanceof Document)
			jobXml = XPath.selectNode("/d:job", jobXml, Pipeline2WS.ns);
		
		id = XPath.selectText("@id", jobXml, Pipeline2WS.ns);
		href = XPath.selectText("@href", jobXml, Pipeline2WS.ns);
		String status = XPath.selectText("@status", jobXml, Pipeline2WS.ns);
		for (Status s : Status.values()) {
			if (s.toString().equals(status)) {
				this.status = s;
				break;
			}
		}
		script.id = XPath.selectText("d:script/@id", jobXml, Pipeline2WS.ns);
		script.href = XPath.selectText("d:script/@href", jobXml, Pipeline2WS.ns);
		script.nicename = XPath.selectText("d:script/d:nicename", jobXml, Pipeline2WS.ns);
		script.desc = XPath.selectText("d:script/d:description", jobXml, Pipeline2WS.ns);
		logHref = XPath.selectText("d:log/@href", jobXml, Pipeline2WS.ns);
		
		List<Node> results = XPath.selectNodes("d:results", jobXml, Pipeline2WS.ns);
		if (results.size() > 0) {
			this.results = JobResult.parseResultXml(results.get(0));
		}
		
		List<Node> messageNodes = XPath.selectNodes("d:messages/d:message", jobXml, Pipeline2WS.ns);
		for (Node messageNode : messageNodes) {
			this.messages.add(new Message(
				XPath.selectText("@level", messageNode, Pipeline2WS.ns),
				XPath.selectText("@sequence", messageNode, Pipeline2WS.ns),
				XPath.selectText(".", messageNode, Pipeline2WS.ns)
			));
		}
		Collections.sort(this.messages);
	}
	
	/**
	 * Parse the list of jobs described by the provided Pipeline2WSResponse.
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Job> getJobs(Pipeline2WSResponse response) throws Pipeline2WSException {
		if (response.status != 200)
			throw new Pipeline2WSException(response.status+" "+response.statusName+": "+response.statusDescription);
		return parseJobsXml(response.asXml());
	}
	
	/**
	 * Parse the list of jobs described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/jobs.xml
	 * 
	 * @param response
	 * @return
	 * @throws Pipeline2WSException
	 */
	public static List<Job> parseJobsXml(Node jobsXml) throws Pipeline2WSException {
		List<Job> jobs = new ArrayList<Job>();
		
		// select root element if the node is a document node
		if (jobsXml instanceof Document)
			jobsXml = XPath.selectNode("/d:jobs", jobsXml, Pipeline2WS.ns);
		
		List<Node> jobNodes = XPath.selectNodes("d:job", jobsXml, Pipeline2WS.ns);
		for (Node jobNode : jobNodes) {
			jobs.add(new Job(jobNode));
		}
		
		return jobs;
	}
}
