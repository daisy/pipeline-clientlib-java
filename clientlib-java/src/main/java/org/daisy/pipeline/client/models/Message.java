package org.daisy.pipeline.client.models;

import java.util.Date;

/** A job message. */
public class Message implements Comparable<Message> {
	
	public enum Level { ERROR, WARNING, INFO, DEBUG, TRACE };
	
	public Level level;
	public Integer sequence;
    public String text;
    public Integer line;
    public Integer column;
    public String timeStamp;
    public String file;
    
    public Date timeStampDate() {
    	// TODO: if timeStamp is actually exposed through the web api (I don't think it currently is), then this method might be useful.
    	return null;
    }

	@Override
	public int compareTo(Message other) {
		return this.sequence - other.sequence;
	}

}
