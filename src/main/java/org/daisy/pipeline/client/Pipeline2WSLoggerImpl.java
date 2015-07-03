package org.daisy.pipeline.client;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Pipeline2WSLoggerImpl implements Pipeline2WSLogger {
	private LEVEL level = LEVEL.INFO;

	public void setLevel(LEVEL level) {
		if (level != null)
			this.level = level;
	}
	
	public boolean logsLevel(LEVEL level) {
		return this.level.ordinal() <= level.ordinal();
	}
	
	public void trace(String message) {
		if (!logsLevel(LEVEL.TRACE)) return;
		System.err.println("[trace] "+message);
	}
	
	public void trace(String message, Exception e) {
		if (!logsLevel(LEVEL.TRACE)) return;
		System.err.println("[trace] "+message);
		System.err.println("[trace] "+stacktraceToString(e));
	}
	
	public void debug(String message) {
		if (!logsLevel(LEVEL.DEBUG)) return;
		System.out.println("[debug] "+message);
	}
	
	public void debug(String message, Exception e) {
		if (!logsLevel(LEVEL.DEBUG)) return;
		System.out.println("[debug] "+message);
		System.err.println("[debug] "+stacktraceToString(e));
	}

	public void info(String message) {
		if (!logsLevel(LEVEL.INFO)) return;
		System.out.println("[info] "+message);
	}
	
	public void info(String message, Exception e) {
		if (!logsLevel(LEVEL.INFO)) return;
		System.out.println("[info] "+message);
		System.err.println("[info] "+stacktraceToString(e));
	}

	public void warn(String message) {
		if (!logsLevel(LEVEL.WARN)) return;
		System.out.println("[warn] "+message);
	}
	
	public void warn(String message, Exception e) {
		if (!logsLevel(LEVEL.WARN)) return;
		System.out.println("[warn] "+message);
		System.err.println("[warn] "+stacktraceToString(e));
	}

	public void error(String message) {
		if (!logsLevel(LEVEL.ERROR)) return;
		System.err.println("[error] "+message);
	}
	
	public void error(String message, Exception e) {
		if (!logsLevel(LEVEL.ERROR)) return;
		System.err.println("[error] "+message);
		System.err.println("[error] "+stacktraceToString(e));
	}

	public void fatal(String message) {
		if (!logsLevel(LEVEL.FATAL)) return;
		System.err.println("[fatal] "+message);
	}
	
	public void fatal(String message, Exception e) {
		if (!logsLevel(LEVEL.FATAL)) return;
		System.err.println("[fatal] "+message);
		System.err.println("[fatal] "+stacktraceToString(e));
	}
	
	public String stacktraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
