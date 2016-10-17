/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common.util;
import io.vertx.core.http.HttpServerResponse;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class RestResponseUtil {
	public static void sendError(int statusCode, String errMsg, HttpServerResponse response) {
		if(errMsg == null)
			response.setStatusCode(statusCode).end();
		else
			response.setStatusCode(statusCode).end(errMsg);
	}
	
	public static void sendError(int statusCode, Throwable err, HttpServerResponse response) {
    	String errMsg = err.getMessage();
		sendError(statusCode, errMsg, response);
	}
}
