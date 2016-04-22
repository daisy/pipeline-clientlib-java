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
    public Long timeStamp = new Date().getTime(); // NOTE: the timeStamp is currently not exposed through the web api so we just set it here to the time the object is instantiated instead.
    public String file;
    
    @Override
	public int compareTo(Message other) {
		return this.sequence - other.sequence;
	}

	public void setTimeStamp(String timeStampString) {
		// TODO timeStamp not exposed through the web api yet. Assume UNIX time for now. See: https://github.com/daisy/pipeline-framework/issues/109
		timeStamp = Long.parseLong(timeStampString);
	}

	public String formatTimeStamp() {
		// TODO timeStamp not exposed through the web api yet. Assume UNIX time for now. See: https://github.com/daisy/pipeline-framework/issues/109
		return ""+timeStamp;
	}
	
	public String getText() {
		String text = this.text;
		if (text != null) {
			// remove progress info from message
			text = text.replaceAll("^\\[[Pp][Rr][Oo][Gg][Rr][Ee][Ss][Ss][^\\]]*\\] *", "");
		}
		return text;
	}

}
