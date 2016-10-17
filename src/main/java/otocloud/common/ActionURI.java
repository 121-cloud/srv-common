/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common;

import io.vertx.core.http.HttpMethod;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
//运行时业务角色
public class ActionURI {
	private String uri;
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	private HttpMethod httpMethod;
	/**
	 * @return the httpMethod
	 */
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}
	/**
	 * @param httpMethod the httpMethod to set
	 */
	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}
	
	public ActionURI(String uri, HttpMethod httpMethod){
		setUri(uri);
		setHttpMethod(httpMethod);
	}
	
}
