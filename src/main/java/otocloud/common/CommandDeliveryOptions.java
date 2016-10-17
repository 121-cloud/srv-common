package otocloud.common;

import java.util.Map;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.JsonObject;

public class CommandDeliveryOptions extends DeliveryOptions {
	private long timeout = Long.MIN_VALUE;
	
	public CommandDeliveryOptions() {
		super();
	}
	
	public CommandDeliveryOptions(CommandDeliveryOptions other) {
		this.setSendTimeout(other.getSendTimeout());
	    this.setCodecName(other.getCodecName());
	    this.setHeaders(other.getHeaders());
	}
	
	public CommandDeliveryOptions(JsonObject json) {
		this.setSendTimeout(json.getLong("timeout", DEFAULT_TIMEOUT));
	    this.setCodecName(json.getString("codecName", null));
	    JsonObject hdrs = json.getJsonObject("headers", null);
	    if (hdrs != null) {
	    	MultiMap headers = new CaseInsensitiveHeaders();
	    	for (Map.Entry<String, Object> entry: hdrs) {
	    		if (!(entry.getValue() instanceof String)) {
	    			throw new IllegalStateException("Invalid type for message header value " + entry.getValue().getClass());
	    		}
	    		headers.set(entry.getKey(), (String)entry.getValue());
	    	}
	    	this.setHeaders(headers);
	    }
	}
	
	@Override
	public CommandDeliveryOptions setSendTimeout(long timeout) {
		Arguments.require(timeout == -1 || timeout >= 1, "sendTimeout must be >= 1 or == -1 for no timeout");	    
		if (timeout >= 1) {
			super.setSendTimeout(timeout);
			this.timeout = timeout;
			return this;
		}
		else {
			this.timeout = timeout;
		    return this;
		}
	}
	
	@Override
	public long getSendTimeout() {
		if (this.timeout != -1) {
			return super.getSendTimeout();
		}
		else {
			return this.timeout;
		}	
	}
}
