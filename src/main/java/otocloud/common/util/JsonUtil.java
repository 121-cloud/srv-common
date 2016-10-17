/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class JsonUtil {
	public static String getJsonValue(JsonObject jsObj, String key){
		String ret = "";
		Object retObject = jsObj.getValue(key);
		if(retObject instanceof String)
			ret = (String)retObject;
		else
			ret = retObject.toString();
		return ret;
	}
	
	public static Buffer writeToBuffer(JsonObject json){
		Buffer buffer = Buffer.buffer();	  			
	    String JsonStr = json.encode();
	    byte[] bytes = JsonStr.getBytes(StandardCharsets.UTF_8);    		    
	    buffer.appendBytes(bytes);
		return buffer;
	}
}
