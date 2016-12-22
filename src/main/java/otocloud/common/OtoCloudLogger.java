/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
public class OtoCloudLogger {
	protected String appInstId;
	protected Logger innerlogger;
	  /**
	 * @return the innerlogger
	 */
	public Logger getLogger() {
		return innerlogger;
	}

	/**
	 * Constructor.
	 *
	 * @param delegate
	 */
	public OtoCloudLogger(String appInstanceId, Logger logger) {
		appInstId = appInstanceId; 
		innerlogger = logger;
	}
	
	  protected String formatMessage(String msg) {
		  return "[" + appInstId + "]:" + msg;		
	  }

	  public boolean isInfoEnabled() {
	    return innerlogger.isInfoEnabled();
	  }

	  public boolean isDebugEnabled() {
	    return innerlogger.isDebugEnabled();
	  }

	  public boolean isTraceEnabled() {
	    return innerlogger.isTraceEnabled();
	  }

	  public void fatal(final String message) {		  
		  innerlogger.fatal(formatMessage(message));
	  }
	  
	  public void fatal(final String message, final Throwable t) {
		  innerlogger.fatal(formatMessage(message), t);
	  }

	  public void error(final String message) {
		  innerlogger.error(formatMessage(message));
	  }

	  public void error(final String message, final Throwable t) {
		  innerlogger.error(formatMessage(message), t);
	  }

	  public void warn(final String message) {
		  innerlogger.warn(formatMessage(message));
	  }

	  public void warn(final String message, final Throwable t) {
		  innerlogger.warn(formatMessage(message), t);
	  }

	  public void info(final String message) {
		  innerlogger.info(formatMessage(message));
	  }

	  public void info(final String message, final Throwable t) {
		  innerlogger.info(formatMessage(message), t);
	  }

	  public void debug(final String message) {
		  innerlogger.debug(formatMessage(message));
	  }

	  public void debug(final String message, final Throwable t) {
		  innerlogger.debug(formatMessage(message), t);
	  }

	  public void trace(final String message) {
		  innerlogger.trace(formatMessage(message));
	  }

	  public void trace(final String message, final Throwable t) {
		  innerlogger.trace(formatMessage(message), t);
	  }
	  
	  //带actor的方法
	  public void fatal(final JsonObject actor, final String message) {		  
		  innerlogger.fatal(formatMessage(actor, message));
	  }
	  
	  public void fatal(final JsonObject actor, final String message, final Throwable t) {
		  innerlogger.fatal(formatMessage(actor, message), t);
	  }

	  public void error(final JsonObject actor, final String message) {
		  innerlogger.error(formatMessage(actor, message));
	  }

	  public void error(final JsonObject actor, final String message, final Throwable t) {
		  innerlogger.error(formatMessage(actor, message), t);
	  }

	  public void warn(final JsonObject actor, final String message) {
		  innerlogger.warn(formatMessage(actor, message));
	  }

	  public void warn(final JsonObject actor, final String message, final Throwable t) {
		  innerlogger.warn(formatMessage(actor, message), t);
	  }

	  public void info(final JsonObject actor, final String message) {
		  innerlogger.info(formatMessage(actor, message));
	  }

	  public void info(final JsonObject actor, final String message, final Throwable t) {
		  innerlogger.info(formatMessage(actor, message), t);
	  }

	  public void debug(final JsonObject actor, final String message) {
		  innerlogger.debug(formatMessage(actor, message));
	  }

	  public void debug(final JsonObject actor, final String message, final Throwable t) {
		  innerlogger.debug(formatMessage(actor, message), t);
	  }

	  public void trace(final JsonObject actor, final String message) {
		  innerlogger.trace(formatMessage(actor, message));
	  }

	  public void trace(final JsonObject actor, final String message, final Throwable t) {
		  innerlogger.trace(formatMessage(actor, message), t);
	  }
	  
	  protected String formatMessage(JsonObject actor, String msg) {
		  return "[" + appInstId + "]-[" + ((actor==null)?"":actor.toString()) + "] " + msg;		
	  }

	}
