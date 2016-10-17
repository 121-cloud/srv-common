package otocloud.common;

import java.time.Instant;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import otocloud.common.Command;

public class CommandResult extends JsonObject {
	private static final Logger log = LoggerFactory.getLogger(CommandResult.class.getName());
	
	public CommandResult(Command cmd) {
//		if (log.isDebugEnabled()) {
			this.put(CommandScheme.COMMAND, new JsonObject().mergeIn(cmd));
//		}
		this.put(CommandScheme.SENDER_ID, cmd.getString(CommandScheme.SENDER_ID));
		this.put(CommandScheme.COMMAND_START_ON, cmd.getInstant(CommandScheme.COMMAND_START_ON));
		this.put(CommandScheme.RESULT_DATA, new JsonArray());	
		if (cmd.isValid()) {			
			this.put(CommandScheme.RESULT_STATUS_CODE, 0);			
			this.put(CommandScheme.RESULT_STATUS_MESSAGE, "");
		}
		else {
			this.put(CommandScheme.RESULT_STATUS_CODE, -1);			
			this.put(CommandScheme.RESULT_STATUS_MESSAGE, "Illegal Command format");
		}
	}
	
	private CommandResult(String json) {
		super(json);
	}
	
	private CommandResult(Object source) {
		JsonObject jsonObj;
		if (source instanceof String) {
			try {
				jsonObj = new JsonObject((String)source);
				initfromJsonObject(jsonObj);
			}
			catch (Exception e) {
				createInvalidCommandResultWrap(source.toString());
			}
		}
		else if (source instanceof JsonObject) {
			jsonObj = (JsonObject)source;
			initfromJsonObject(jsonObj);
		}
		else{
			createInvalidCommandResultWrap(source.toString());
		}
	}	
			
	public static CommandResult fromMessageBody(Object body) {
		if (body instanceof CommandResult) {
			return (CommandResult)body;
		}
		else {
			return new CommandResult(body);
		}
	}
	
	public static CommandResult fromJson(String json) {
		try {
			CommandResult result = new CommandResult(json);
			if (!result.isValid()) {
				result.createInvalidCommandResultWrap(json);				
			}
			return result;
		}
		catch (Exception e) {
			return new CommandResult((Object)json);
		}
	}
	
	public static CommandResult fromJsonObject(JsonObject other) {
		return new CommandResult(other);
	}
	
	private void initfromJsonObject(JsonObject source) {
		if (assertValid(source)) {
			this.mergeIn(source);
		}
		else {
			createInvalidCommandResultWrap(source.toString());
		}	
	}
	public CommandResult reBaseProgress(int lastProgress) {
		int total = this.getTotal();
		int delta = this.getDelta();
		int progress = lastProgress + delta;
		this.put(CommandScheme.PROGRESS, progress);
		if (progress == total) {
			this.put(CommandScheme.RESULT_STATUS_CODE, 0);
		}
		else {
			this.put(CommandScheme.RESULT_STATUS_CODE, 100);
		}
		return this;
	}
	public CommandResult progress(int total, int delta, int progress, JsonArray datas, String message, int sequenceID) {
		if (progress == total) {
			this.put(CommandScheme.RESULT_STATUS_CODE, 0);
		}
		else {
			this.put(CommandScheme.RESULT_STATUS_CODE, 100);
		}
		this.put(CommandScheme.RESULT_STATUS_MESSAGE, message);
		this.put(CommandScheme.PROGRESS, progress);
		this.put(CommandScheme.DELTA, delta);
		this.put(CommandScheme.TOTAL, total);
		this.put(CommandScheme.SEQUENCEID, sequenceID);
		
		this.addData(datas);
		return this;
	}
	
	public CommandResult succeed(){
		this.put(CommandScheme.RESULT_STATUS_CODE, 0);
//		this.put(CommandScheme.TOTAL, this.getDataCount());
//		this.put(CommandScheme.DELTA, this.getDataCount());
//		this.put(CommandScheme.PROGRESS, this.getDataCount());
		return this;
	}
	
	public CommandResult succeed(JsonObject data){
		return this.addData(data).succeed();		
	}
	
	public CommandResult succeed(JsonArray datas){
		return this.addData(datas).succeed();	
	}
	
	public CommandResult fail(String errMsg){
		return fail(-1, errMsg);
	}
	
	public CommandResult fail(int errCode, String errMsg){
		this.put(CommandScheme.RESULT_STATUS_CODE, errCode);
		this.put(CommandScheme.RESULT_STATUS_MESSAGE, errMsg);
		return this;
	}
	
	public CommandResult fail(Throwable e) {
		if (log.isDebugEnabled() && !(e instanceof ReplyException)) {
			e.printStackTrace();
			this.put(CommandScheme.RESULT_STACK_TRACE, printStackTrace(e));
		}
		if (e instanceof ReplyException) {
			ReplyException re = (ReplyException)e;
			this.put(CommandScheme.RESULT_STATUS_CODE, re.failureCode());
			this.put(CommandScheme.RESULT_STATUS_MESSAGE, re.getMessage());
		}
		else {
			this.put(CommandScheme.RESULT_STATUS_CODE, -1);
			this.put(CommandScheme.RESULT_STATUS_MESSAGE, e.getMessage());
		}
		return this;
	}
	
	public Command getCommand(){
		if (this.isValid()) {
			return Command.fromJsonObject(this.getJsonObject(CommandScheme.COMMAND));
		}
		else {
			return null;
		}	
	}
	
	public String getSenderID() {
		return this.getString(CommandScheme.SENDER_ID);
	}
	
	public int getStatusCode(){
		return this.getInteger(CommandScheme.RESULT_STATUS_CODE);			
	}
		
	public int getDataCount() {
		return this.getJsonArray(CommandScheme.RESULT_DATA).size();
	}
	
	public JsonArray getDatas(){
		JsonArray datas = this.getJsonArray(CommandScheme.RESULT_DATA);
		if (datas == null) {
			datas = new JsonArray();
			this.setDatas(datas);
		}
		return datas; 
	}
	
	public CommandResult setDatas(JsonArray datas) {
		this.put(CommandScheme.RESULT_DATA, datas);
		return this;
	}
	
	public JsonObject getData() {
		 return this.getData(0);
	}
	
	public JsonObject getData(int index) {
		Arguments.require(index >=0 && index < this.getDataCount(), "index out of range");
		return this.getDatas().getJsonObject(index);
	}
	
	public CommandResult addData(JsonObject item) {
		this.getDatas().add(item);
		return this;
	}
	
	public CommandResult addData(JsonArray items) {
		items.forEach(item -> {
			this.getDatas().add(item);
		});		
		return this;
	}	
	
	public String getStatusMessage(){
		return this.getString(CommandScheme.RESULT_STATUS_MESSAGE);					
	}
	
	public String getStackTrace() {
		return this.getString(CommandScheme.RESULT_STACK_TRACE, "");
	}
	
	public int getTotal() {
		return this.getInteger(CommandScheme.TOTAL, 0);		
	}
	
	public int getDelta() {
		return this.getInteger(CommandScheme.DELTA, 0);
	}
	
	public int getProgress() {
		return this.getInteger(CommandScheme.PROGRESS, 0);
	}
		
	public int getProgressPercent() {
		int total = getTotal();
		if (total == 0) return 100;
		return (int)((getProgress() * 100) / total);
	}
	
	public int getSequenceID() {
		return this.getInteger(CommandScheme.SEQUENCEID, -1);
	}
		
	public CommandDeliveryOptions getDeliveryOptions() {
		JsonObject json = this.getJsonObject(CommandScheme.DELIVERYOPTIONS, null);
		CommandDeliveryOptions options;
		if (json == null) {
			options = new CommandDeliveryOptions();
		}
		else {
			options = new CommandDeliveryOptions(json);
		}
		return options;
	}
	
	public CommandResult setDeliveryOptions(CommandDeliveryOptions options) {
		JsonObject json = new JsonObject();
		json.put("sendTimeOut", options.getSendTimeout());
		json.put("codecName", options.getCodecName());
		this.put(CommandScheme.DELIVERYOPTIONS, json);
		
		return this;
	}
	
	public boolean isValid() {
		return assertValid(this) && !this.isInvalidCommandResultWrap();
	}
	
	public boolean succeeded() {
		return (this.getStatusCode() == 0 || this.getStatusCode() == 100);
	}
	
	public boolean failed() {
		return !(succeeded());
	}
	
	public boolean inProgress() {
		return (this.getStatusCode() == 100);
	}
	
	public boolean isComplete() {
		return !(inProgress());
	}
	
	public Instant getReplyOn() {
		return this.getInstant(CommandScheme.RESULT_REPLY_ON, Instant.now());
	}
	
	public Instant getStartOn() {
		return this.getInstant(CommandScheme.COMMAND_START_ON);
	}
	
	public long duration() {
		return getReplyOn().toEpochMilli() - getStartOn().toEpochMilli();		
	}
	
	public boolean isInvalidCommandResultWrap() {
		return this.getStatusCode() == -2;
	}
	
	public static boolean assertValid(String json) {
		try {
			return assertValid(new JsonObject(json));
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static boolean assertValid(JsonObject jsonObj) {
		boolean result = true;
		
		result = result && jsonObj.containsKey(CommandScheme.RESULT_STATUS_CODE);
		result = result && jsonObj.containsKey(CommandScheme.RESULT_DATA);
//		if (log.isDebugEnabled()) {
//			result = result && jsonObj.containsKey(CommandScheme.COMMAND);
//			result = result && Command.assertValid(jsonObj.getJsonObject(CommandScheme.COMMAND));
//		}
		
		if(!result) {
			log.debug("invalid CommandResult format:" + jsonObj.toString());
		}
		return result;
	}
	
	private void createInvalidCommandResultWrap(String invalidCommandResultRaw) {		
		this.put(CommandScheme.COMMAND, new JsonObject());
		this.put(CommandScheme.RESULT_STATUS_CODE, -2);
		this.put(CommandScheme.RESULT_DATA, new JsonObject());
		this.put(CommandScheme.RESULT_STATUS_MESSAGE, "illegal result format");
		this.put(CommandScheme.RAW_INVALID_RESULT, invalidCommandResultRaw);
		log.debug("illegal commandresult format: " + invalidCommandResultRaw);
	}
	
	private String printStackTrace(Throwable e) {
		StringBuilder stacktrace = new StringBuilder();
		StackTraceElement[] ste = e.getStackTrace();
		stacktrace.append(e.toString()).append("\n");
		for (int i = 0; i < ste.length; i++) {
			stacktrace.append("\tat ").append(ste[i].toString()).append("\n");
		}
		return stacktrace.toString();
	}
}