/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */

public class OtoConfiguration {
	public static final String CLUSTER_CFG = "cluster_config";
	public static final String CLUSTER_HOST = "cluster_host";
	public static final String CLUSTER_PORT = "cluster_port";	
	
	public static final String WEBSERVER_HOST = "run_webserver"; //是否作为web服务器HOST
	
	public static final String COMPONENT_DEPLOY = "component_deployment";
	public static final String COMPONENT_COMMON = "component_common";
	public static final String COMPONENT_CFG = "component_config";
	
	//----web服务器配置----
	public static final String WEBSERVER_CFG = "webserver_config"; 
	public static final String WEBSERVER_PORT = "webserver_port"; //web服务器端口
	public static final String EB_DELIVERY_OPTIONS = "delivery_options";//设置event bus的发送配置
	
	public static final String EVENTBUS_ENABLED = "eventbus_enabled"; //是否启用web事件总线
	public static final String STATIC_RES_SERVICE = "static_res_service"; //是否静态资源服务
	//------------------
	
	public static final String CLUSTER_CFG_FILE = "hazelcast.xml";
	
	public static final String SYS_DATASOURCE = "sys_datasource";
	
	public static final String SHAREDPOOL = "sharedpool";
	public static final String CONFIG = "config";	
	
	public static final String VERTX_OPTIONS_KEY = "vertx_options";
	
}
