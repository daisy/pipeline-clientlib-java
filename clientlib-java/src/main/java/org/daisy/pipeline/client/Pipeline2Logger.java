package org.daisy.pipeline.client;

public interface Pipeline2Logger {
	/**
	 * Logger levels.
	 * ALL > TRACE > DEBUG > INFO > WARN > ERROR > FATAL > OFF.
	 * ALL -The ALL Level has the lowest possible rank and is intended to turn on all logging. In practice the same as the TRACE level.
	 * TRACE - The TRACE Level designates finer-grained informational events than the DEBUG level.
	 * DEBUG - The DEBUG Level designates fine-grained informational events that are most useful to debug an application.
	 * INFO – The INFO level designates informational messages that highlight the progress of the application at coarse-grained level.
	 * WARN – The WARN level designates potentially harmful situations.
	 * ERROR – The ERROR level designates error events that might still allow the application to continue running.
	 * FATAL – The FATAL level designates very severe error events that will presumably lead the application to abort.
	 * OFF – The OFF Level has the highest possible rank and is intended to turn off logging.
	 */
	public enum LEVEL {
		ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
	}
	
	/** Set the logger level. */
	public void setLevel(LEVEL level);
	
	/** Returns whether or not messages of the given level will be logged. */
	public boolean logsLevel(LEVEL level);
	
	/** The TRACE Level designates finer-grained informational events than the DEBUG level. */
	public void trace(String message);
	public void trace(String message, Exception e);
	
	/** The DEBUG Level designates fine-grained informational events that are most useful to debug an application. */
	public void debug(String message);
	public void debug(String message, Exception e);
	
	/** The INFO level designates informational messages that highlight the progress of the application at coarse-grained level. */
	public void info(String message);
	public void info(String message, Exception e);
	
	/** The WARN level designates potentially harmful situations. */
	public void warn(String message);
	public void warn(String message, Exception e);
	
	/** The ERROR level designates error events that might still allow the application to continue running. */
	public void error(String message);
	public void error(String message, Exception e);
	
	/** The FATAL level designates very severe error events that will presumably lead the application to abort. */
	public void fatal(String message);
	public void fatal(String message, Exception e);
}
