package org.daisy.pipeline.client;

import java.util.List;

import org.daisy.pipeline.client.models.Job;

/**
 * Methods for communicating with the Pipeline 2 API.
 * 
 * @see http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI
 */
public abstract class Pipeline2WS {
	
	/** Delete a client */
//	public abstract String deleteClient(Client client);
	
	/** Get a client */
//	public abstract Client getClient(String clientId);
	
	/** List all clients */
//	public abstract List<Client> getClients();
	
	/** Stop the web service */
	public abstract String halt(String key);
	
	/** Get  */
//	public abstract Properties getProperties();
//	public abstract Sizes getSizes();
//	public abstract String postClients(TODO);
//	public abstract String putClient(Client client);
	
	/** Get information about the framework */
	public abstract String alive();
//	public abstract String deleteBatch(Batch batch);
//	public abstract Batch getBatch(String batchId);

	/** Delete a single job */
	public abstract String deleteJob(Job job);
	public abstract Job getJob(String jobId, String msgSeq);

	/** Get a single job */
	public abstract Job getJob(String jobId);
	
	/** Get all jobs */
	public abstract String getJobs();

	/** Get the log file for a job */
	public abstract String getJobLog(String jobId);
	public String getJobLog(Job job) {
		return getJobLog(job.getId());
	}

	/** Get the result for a job */
	public abstract String getJobResult(String jobId);
	
	/** Create a job with files */
	public abstract String postJob();
	
	/** Move job up the queue */
	public abstract String getQueue();
	
	/** Move job up the queue */
	public abstract String moveDownQueue(String jobId);

	/** Move job up the queue */
	public abstract String moveUpQueue(String jobId);

	/** Get a single script */
	public abstract String getScript(String scriptId);

	/** Get all scripts */
	public abstract String getScripts();

}
