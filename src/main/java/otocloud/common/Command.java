package otocloud.common;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Command extends JsonObject {	
	private static final Logger log = LoggerFactory.getLogger(Command.class.getName());
	
	private Handler<CommandResult> replyHandler;
	private MessageConsumer<Object> replyListener = null;
	private MessageConsumer<Object> commandConsumer = null;	
	private Message<?> messageCarrier = null;
	private long timeoutID = -1;
	private URI absURI = null;
	
	private int sendSequenceID = 0;
	private int sendProgress = 0;
	private int sendTotal = 0;
	
	private int currentReceiveSequenceID = 0;
	private int receiveProgress = 0;
	private ConcurrentMap<Integer, CommandResult> receiveBuffer = new ConcurrentHashMap<>();
	
	public Command(int accountID, String address) {
		this(accountID, address, "");
	}
	
	public Command(int accountID, String address, String action) {
		this.put(CommandScheme.ACCOUNT_ID, accountID);		
		this.put(CommandScheme.ADDRESS, address);
		if (action != null && !action.equals("")) {
			this.put(CommandScheme.ACTION, action);
		}
		this.put(CommandScheme.SENDER_ID, UUID.randomUUID().toString());
		this.put(CommandScheme.PARAMS, new JsonObject());
		this.put(CommandScheme.CONTENT, new JsonArray());
		this.put(CommandScheme.SESSION, new JsonObject());
	}
	
	private Command(String json) {		
		super(json);		
	}
	
	private Command(Object source) {
		JsonObject jsonObj;
		if (source instanceof String) {		
			try {
				jsonObj = new JsonObject((String)source);
				initfromJsonObject(jsonObj);
			}
			catch (Exception e) {
				createInvalidCommandWrap(source.toString());
			}
		}
		else if (source instanceof JsonObject) {
			jsonObj = (JsonObject)source;
			initfromJsonObject(jsonObj);
		}
		else{
			createInvalidCommandWrap(source.toString());
		}
	}	
	
	public static Command fromMessageBody(Object body) {
		if (body instanceof Command) {
			return (Command)body;
		}
		else {
			return new Command(body);
		}		
	}
	
	public static Command fromJsonObject(JsonObject other) {
		return new Command(other);
	}
	
	public static Command fromJson(String json) {
		try {
			Command cmd = new Command(json);
			if (!assertValid(cmd)) {
				cmd.createInvalidCommandWrap(json);
			}
			return cmd;
		}
		catch(Exception e) {
			return new Command((Object)json);
		}
	}
	
	private void initfromJsonObject(JsonObject source) {
		if (assertValid(source)) {
			this.mergeIn(source);			
		}
		else {
			createInvalidCommandWrap(source.toString());
		}	
	}
	
	public Command setMessageCarrier(Message<?> msg) {
		Objects.requireNonNull(msg, "msg");
		messageCarrier = msg;
		if (this.canMultiTimeReply()) {
			msg.reply("received");
			setMessageCarrierReplyAddress(messageCarrier, this.getReplyAddress());
		}
		else {
			this.put(CommandScheme.REPLYADDRESS, msg.replyAddress() != null ? msg.replyAddress() : "");
		}
		return this;
	}
	
	public String getGatewayAddress() {
		return CommandScheme.GATEWAY_ADDRESS_PREFIX + String.valueOf(getAccountID());						
	}
	
	public int getAccountID() {
		return this.getInteger(CommandScheme.ACCOUNT_ID);				
	}
	
	public String getCommandAddress() {
		return this.getString(CommandScheme.ADDRESS);			
	}
	
	public String getSenderID() {
		return this.getString(CommandScheme.SENDER_ID);
	}	
	
	public String getFromServiceName() {
		return this.getString(CommandScheme.FROM_SERVICE_NAME, null);
	}
	
	public void setFromServiceName(String fromService) {
		this.put(CommandScheme.FROM_SERVICE_NAME, fromService);
	}
	
	public String getReplyAddress() {
		return this.getString(CommandScheme.REPLYADDRESS, "");
	}
	
	public String getAction() {
		return this.getString(CommandScheme.ACTION, "");
	}
	
	public JsonObject getParams() {
		return this.getJsonObject(CommandScheme.PARAMS);
	}
	
	public Command setParams(JsonObject params) {
		this.put(CommandScheme.PARAMS, params);
		return this;
	}
	
	public Command addParam(String key, Object value) {
		this.getParams().put(key, value);
		return this;
	}	
	
	public int getContentsCount() {
		return this.getContents().size();
	}
	
	public JsonObject getContent() {
		return this.getContent(0);
	}
	
	public JsonObject getContent(int index) {
		Arguments.require(index >=0 && index < this.getContentsCount(), "index out of range");
		return this.getContents().getJsonObject(index);
	}
	
	public JsonArray getContents() {
		JsonArray contents = this.getJsonArray(CommandScheme.CONTENT);
		if (contents == null) {
			contents = new JsonArray();
			this.setContents(contents);
		}
		return contents;
	}
	
	public void setContents(JsonArray contents) {
		this.put(CommandScheme.CONTENT, contents);
	}
	
	public Command addContent(JsonObject item) {
		this.getContents().add(item);
		return this;
	}
	
	public Command addContent(JsonArray items) {
		items.forEach(item -> {
			this.getContents().add(item);
		});
		return this;
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
	
	public Command setDeliveryOptions(CommandDeliveryOptions options) {
		JsonObject json = new JsonObject();
		json.put("timeout", options.getSendTimeout());
		json.put("codecName", options.getCodecName());
		this.put(CommandScheme.DELIVERYOPTIONS, json);
		
		return this;
	}
	
	public JsonObject getSessions() {
		JsonObject sessions = this.getJsonObject(CommandScheme.SESSION);
		if (sessions == null) {
			sessions = new JsonObject();
			this.setSessions(sessions);
		}
		return sessions;
	}
	
	public Command setSessions(JsonObject sessions) {
		this.put(CommandScheme.SESSION, sessions);
		return this;
	}
	
	public String getSessionID() {
		JsonObject sessions = this.getSessions();
		if (sessions != null) {
			return sessions.getString(CommandScheme.SESSION_ID);
		}
		else {
			return null;
		}
	}
	
	public Command setSessionID(String sessionID) {
		this.getSessions().put(CommandScheme.SESSION_ID, sessionID);
		return this;
	}
	
	public JsonObject getRestAPIDef() {
		return this.getJsonObject(CommandScheme.REST_API);
	}
	
	public Command setRestAPIDef(JsonObject apidef) {
		Objects.requireNonNull(apidef, "apidef");
		Arguments.require(apidef.containsKey("uri") && apidef.containsKey("method"), "apidef must include apiuri & method properties");
		this.put(CommandScheme.REST_API, apidef);
		return this;
	}
	
	public URI getRequestURI() {
		if (absURI == null) {
			try{
				String absURIStr = this.getString(CommandScheme.ABS_URI);
				absURI = new URI(absURIStr);
			}
			catch(Exception e) {
				absURI = null;
			}
		}
		return absURI;
	}
	
	public String getAbsURI() {
		return this.getString(CommandScheme.ABS_URI);
	}
	
	public String getURI() {
		return this.getRequestURI().getPath() + "?" + this.getRequestURI().getQuery();
	}
	
	public String getPath() {
		URI uri = this.getRequestURI();
		if (uri != null) {
			return uri.getPath();
		}
		else {
			return null;
		}
	}
	
	public Command setRequestURI(String absuri) {
		try{
			absURI = new URI(absuri);
			this.put(CommandScheme.ABS_URI, absuri);
		}
		catch(Exception e) {
			log.debug("illegal format for AbsURI:" + absuri);
		}
		return this;
	}
	
	public CommandResult createResultObject() {
		return new CommandResult(this);
	}
	
	public Instant getStartOn() {
		return this.getInstant(CommandScheme.COMMAND_START_ON);		
	}
	
	public boolean isValid() {
		return assertValid(this) && !this.isInvalidCommandWrap();
	}
	
	public boolean isInvalidCommandWrap() {
		return this.getString(CommandScheme.ADDRESS).equals(CommandScheme.INVALID_COMMAND);
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
				
		result = result && jsonObj.containsKey(CommandScheme.ACCOUNT_ID);
		result = result && jsonObj.containsKey(CommandScheme.ADDRESS);
		result = result && jsonObj.getString(CommandScheme.ADDRESS, "") != "";
				
		return result;
	}
	
	public void registryConsumer(EventBus ebus, Handler<Command> handler) {
		commandConsumer = Command.consumer(ebus, this.getCommandAddress(), handler);		
	}
	
	public void registryConsumer(Vertx vertx, Handler<Command> handler) {
		commandConsumer = Command.consumer(vertx, this.getCommandAddress(), handler);		
	}
	
	public void unRegistryConsumer() {
		if (commandConsumer != null) {
			commandConsumer.unregister();
		}
	}
	
	public static MessageConsumer<Object> consumer(Vertx vertx, String address, Handler<Command> handler) {
		Objects.requireNonNull(vertx, "vertx");
		return consumer(vertx.eventBus(), address, handler);
	}
	
	public static MessageConsumer<Object> consumer(EventBus ebus, String address, Handler<Command> handler) {
		Objects.requireNonNull(handler, "handler");
		Objects.requireNonNull(ebus, "EventBus");
		
		MessageConsumer<Object> commandConsumer = ebus.consumer(address);
		commandConsumer.handler(msg -> {			
			handler.handle(Command.fromMessageBody(msg.body()).setMessageCarrier(msg));			
		});
		return commandConsumer;
	}	
	
	public boolean canMultiTimeReply() {
		return this.getBoolean(CommandScheme.MULTITIMEREPLY, false);
	}
	
	public void updateProgress(Vertx vertx, int total, int delta) {
		updateProgress(vertx, total, delta, new JsonArray(), "");
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, String message) {
		updateProgress(vertx, total, delta, new JsonArray(), message);
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonObject data) {
		updateProgress(vertx, total, delta, new JsonArray().add(data), "");
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonArray datas) {
		updateProgress(vertx, total, delta, datas, "");
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonObject data, String message) {
		updateProgress(vertx, total, delta, new JsonArray().add(data), message, sendSequenceID++);
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonArray datas, String message) {
		updateProgress(vertx, total, delta, datas, message, sendSequenceID++);
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonArray datas, int sequenceID) {
		updateProgress(vertx, total, delta, datas, "", sequenceID);
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonObject data, String message, int sequenceID) {
		updateProgress(vertx, total, delta, new JsonArray().add(data), message, sequenceID);
	}
	
	public void updateProgress(Vertx vertx, int total, int delta, JsonArray datas, String message, int sequenceID) {
		Objects.requireNonNull(vertx, "vertx");
		
		updateProgress(vertx.eventBus(), total, delta, datas, message, sequenceID);
	}
	
	public void updateProgress(EventBus ebus, int total, int delta) {
		updateProgress(ebus, total, delta, new JsonArray(), "");
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, String message) {
		updateProgress(ebus, total, delta, new JsonArray(), message);
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonObject data) {
		updateProgress(ebus, total, delta, new JsonArray().add(data), "");
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonArray datas) {
		updateProgress(ebus, total, delta, datas, "");
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonObject data, String message) {
		updateProgress(ebus, total, delta, new JsonArray().add(data), message, sendSequenceID++);
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonArray datas, String message) {
		updateProgress(ebus, total, delta, datas, message, sendSequenceID++);
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonArray datas, int sequenceID) {
		updateProgress(ebus, total, delta, datas, "", sequenceID);
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonObject data, String message, int sequenceID) {
		updateProgress(ebus, total, delta, new JsonArray().add(data), message, sequenceID);
	}
	
	public void updateProgress(EventBus ebus, int total, int delta, JsonArray datas, String message, int sequenceID) {
		if (!this.canMultiTimeReply()) {
			log.error("Command Sender did not request multitimeReply Style, updateProgress method can't be used!");
			return;
		}
		
		Objects.requireNonNull(ebus, "EventBus");
		Objects.requireNonNull(datas, "datas");
		Arguments.require(sendTotal == 0 || sendTotal == total, "can't change the total value in progressing");
		Arguments.require((total > 0 && delta > 0) || (total ==0 && delta ==0), "delta must be greater than 0");
		Arguments.require(total >= delta + sendProgress, "the sended total: " + String.valueOf(sendProgress + delta) + " can't be greater than expected total: " + String.valueOf(total));
		
		sendSequenceID = sequenceID + 1;
		this.sendProgress += delta;
		this.sendTotal = total;
		
		CommandResult result = this.createResultObject();
		result.progress(total, delta, sendProgress, datas, message, sequenceID);	
		log.info("send progress: sequenceID=" + String.valueOf(result.getSequenceID()) + " delta=" + String.valueOf(result.getDelta()) + " progress=" + String.valueOf(result.getProgress()) + " total=" + String.valueOf(result.getTotal()));
		reply(ebus, result);		
	}
	
	public void succeed(Vertx vertx) {
		succeed(vertx, new JsonObject());
	}
			
	public void succeed(Vertx vertx, JsonObject data) {
		succeed(vertx, new JsonArray().add(data));
	}
	
	public void succeed(Vertx vertx, JsonArray datas) {		
		Objects.requireNonNull(vertx, "vertx");
		succeed(vertx.eventBus(), datas);
	}
	
	public void succeed(EventBus ebus) {
		succeed(ebus, new JsonObject());
	}
	
	public void succeed(EventBus ebus, JsonObject data) {
		succeed(ebus, new JsonArray().add(data));
	}
	
	public void succeed(EventBus ebus, JsonArray datas) {		
		CommandResult result = this.createResultObject();
		result.succeed(datas);
		reply(ebus, result);
	}
	
	public void fail(Vertx vertx, String errMsg) {
		fail(vertx, -1, errMsg);
	}
	
	public void fail(Vertx vertx, int errCode, String errMsg) {
		fail(vertx, errCode, errMsg, new JsonObject());
	}
	
	public void fail(Vertx vertx, int errCode, String errMsg, JsonObject data) {
		fail(vertx, errCode, errMsg, new JsonArray().add(data));
	}
	
	public void fail(Vertx vertx, int errCode, String errMsg, JsonArray datas) {				
		Objects.requireNonNull(vertx, "vertx");
		fail(vertx.eventBus(), errCode, errMsg, datas);
	}
	
	public void fail(Vertx vertx, Throwable cause) {			
		fail(vertx.eventBus(), cause);	
	}
	
	public void fail(EventBus ebus, String errMsg) {
		fail(ebus, -1, errMsg);
	}
	
	public void fail(EventBus ebus, int errCode, String errMsg) {
		fail(ebus, errCode, errMsg, new JsonObject());
	}
	
	public void fail(EventBus ebus, int errCode, String errMsg, JsonObject data) {
		fail(ebus, errCode, errMsg, new JsonArray().add(data));
	}
	
	public void fail(EventBus ebus, int errCode, String errMsg, JsonArray datas) {				
		Arguments.require((errCode != 0 && errCode != 100), "errCode can not be 0 or 100 (they are represented succeeded &inProgress)");
		
		CommandResult result = this.createResultObject();
		result.addData(datas);
		result.fail(errCode, errMsg);
		reply(ebus, result);
	}
	
	public void fail(EventBus ebus, Throwable cause) {		
		CommandResult result = this.createResultObject();
		result.fail(cause);		
		reply(ebus, result);	
	}
	
	public void reply(Vertx vertx, CommandResult result) {
		reply(vertx, result.getDeliveryOptions(), result);
	}
	
	public void reply(Vertx vertx, CommandDeliveryOptions options, CommandResult result) {
		reply(vertx, options, result, null);
	}
	
	public void reply(Vertx vertx, CommandResult result, Handler<CommandResult> handler) {
		reply(vertx, result.getDeliveryOptions(), result, handler);
	}
		
	public void reply(Vertx vertx, CommandDeliveryOptions options, CommandResult result, Handler<CommandResult> handler) {
		Objects.requireNonNull(vertx, "vertx");
		reply(vertx.eventBus(), options, result, handler);
	}
	
	public void reply(EventBus ebus, CommandResult result) {
		reply(ebus, result.getDeliveryOptions(), result);
	}	
	
	public void reply(EventBus ebus, CommandDeliveryOptions options, CommandResult result) {
		reply(ebus, options, result, null);
	}
	
	public void reply(EventBus ebus, CommandResult result, Handler<CommandResult> handler) {
		reply(ebus, result.getDeliveryOptions(), result, handler);
	}		
	
	public void reply(EventBus ebus, CommandDeliveryOptions options, CommandResult result, Handler<CommandResult> handler) {
		if (this.getReplyAddress().equals("")) {
			log.warn("None ReplyAddress for the command:" + this.toString());
			return;
		}
		Objects.requireNonNull(ebus,"EventBus");
		Arguments.require(this.getSenderID().equals(result.getSenderID()), "the result is not for this Command");
		Arguments.require(result.isValid(), "the result is not valid");
		
		//Command request multiReply style, but you used succeed or reply or fail method response
		if (this.canMultiTimeReply() && result.getSequenceID() == -1) {
			result = endUpdateProgress(result);
		}		
		
		result.put(CommandScheme.RESULT_REPLY_ON, getNow());
		options.setCodecName("CommandResult");		
		options = adjustToEventBusCurrentCodec(ebus, options);
		result.setDeliveryOptions(options);
		
		if (handler != null) {
			messageCarrier.reply(result, options, ar -> {
				CommandResult rep;
				if (ar.succeeded()) {
					rep = CommandResult.fromMessageBody(ar.result().body());
				}
				else {
					rep = this.createResultObject();
					rep.fail(ar.cause());
				}
				handler.handle(rep);
			});
		}
		else {			
			messageCarrier.reply(result, options);
		}
	}
	
	public void execute(Vertx vertx) {		
		this.execute(vertx, null);
	}
			
	public void execute(Vertx vertx, Handler<CommandResult> replyHandler) {		
		this.execute(vertx, false, replyHandler);
	}
	
	public void execute(Vertx vertx, boolean multiTimeReply, Handler<CommandResult> replyHandler) {		
		this.execute(vertx, this.getDeliveryOptions(), multiTimeReply, replyHandler);
	}
	
	public void execute(Vertx vertx, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.execute(vertx, options, false, replyHandler);
	}
		
	public void execute(Vertx vertx, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {		
		this.executeCommand(vertx, this.getCommandAddress(), options, multiTimeReply, replyHandler);
	}
	
	public void execute(EventBus ebus) {		
		this.execute(ebus, null);
	}
			
	public void execute(EventBus ebus, Handler<CommandResult> replyHandler) {		
		this.execute(ebus, false, replyHandler);
	}
	
	public void execute(EventBus ebus, boolean multiTimeReply, Handler<CommandResult> replyHandler) {		
		this.execute(ebus, this.getDeliveryOptions(), multiTimeReply, replyHandler);
	}
	
	public void execute(EventBus ebus, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.execute(ebus, options, false, replyHandler);
	}
		
	public void execute(EventBus ebus, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {		
		Objects.requireNonNull(ebus, "EventBus");
		Vertx vertx = this.getVertxofEventBus(ebus);
		
		this.executeCommand(vertx, this.getCommandAddress(), options, multiTimeReply, replyHandler);
	}
	
	public void executeOnGateway(Vertx vertx) {
		this.executeOnGateway(vertx, null);
	}
	
	public void executeOnGateway(Vertx vertx, Handler<CommandResult> replyHandler) {
		this.executeOnGateway(vertx, false, replyHandler);
	}
	
	public void executeOnGateway(Vertx vertx, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		this.executeOnGateway(vertx, getDeliveryOptions(), multiTimeReply, replyHandler);
	}
	
	public void executeOnGateway(Vertx vertx, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.executeOnGateway(vertx, options, false, replyHandler);
	}
	
	public void executeOnGateway(Vertx vertx, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		this.executeCommand(vertx, this.getGatewayAddress(), options, multiTimeReply, replyHandler);
	}
	
	public void executeOnGateway(EventBus ebus) {
		this.executeOnGateway(ebus, null);
	}
	
	public void executeOnGateway(EventBus ebus, Handler<CommandResult> replyHandler) {
		this.executeOnGateway(ebus, false, replyHandler);
	}
	
	public void executeOnGateway(EventBus ebus, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		this.executeOnGateway(ebus, getDeliveryOptions(), multiTimeReply, replyHandler);
	}
	
	public void executeOnGateway(EventBus ebus, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.executeOnGateway(ebus, options, false, replyHandler);
	}
	
	public void executeOnGateway(EventBus ebus, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		Objects.requireNonNull(ebus, "EventBus");
		Vertx vertx = this.getVertxofEventBus(ebus);
		
		this.executeCommand(vertx, this.getGatewayAddress(), options, multiTimeReply, replyHandler);
	}
	
	public void executeOnServer(Vertx vertx) {
		this.executeOnServer(vertx, null);
	}
	
	public void executeOnServer(Vertx vertx, Handler<CommandResult> replyHandler) {
		this.executeOnServer(vertx, false, replyHandler);
	}
	
	public void executeOnServer(Vertx vertx, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		this.executeOnServer(vertx, getDeliveryOptions(), multiTimeReply, replyHandler);
	}
	
	public void executeOnServer(Vertx vertx, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.executeOnServer(vertx, options, false, replyHandler);
	}
	
	public void executeOnServer(Vertx vertx, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		this.executeCommand(vertx, this.getGatewayAddress(), options, multiTimeReply, replyHandler);
	}
	
	public void executeOnServer(EventBus ebus) {
		this.executeOnServer(ebus, null);
	}
	
	public void executeOnServer(EventBus ebus, Handler<CommandResult> replyHandler) {
		this.executeOnServer(ebus, false, replyHandler);
	}
	
	public void executeOnServer(EventBus ebus, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		this.executeOnServer(ebus, getDeliveryOptions(), multiTimeReply, replyHandler);
	}
	
	public void executeOnServer(EventBus ebus, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.executeOnServer(ebus, options, false, replyHandler);
	}
	
	public void executeOnServer(EventBus ebus, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		Objects.requireNonNull(ebus, "EventBus");
		Vertx vertx = this.getVertxofEventBus(ebus);
		
		this.executeCommand(vertx, this.getGatewayAddress(), options, multiTimeReply, replyHandler);
	}
		
	protected void executeCommand(Vertx vertx, String address, CommandDeliveryOptions options, boolean multiTimeReply, Handler<CommandResult> replyHandler) {
		Objects.requireNonNull(vertx, "vertx");
					
		this.put(CommandScheme.COMMAND_START_ON, getNow());
		options.setCodecName("Command");				
		options = adjustToEventBusCurrentCodec(vertx.eventBus(), options);
		this.setDeliveryOptions(options);	
		
		if (multiTimeReply || options.getSendTimeout() == -1) {
			executeCommandWithMultiTimeReply(vertx, address, options, replyHandler);
		}
		else {
			executeCommandWithSingleReply(vertx, address, options, replyHandler);
		}		
	}	
	
	private void executeCommandWithMultiTimeReply(Vertx vertx, String address, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.replyHandler = replyHandler;
		this.put(CommandScheme.MULTITIMEREPLY, true);
		
		receiveProgress = 0;
		receiveBuffer.clear();		
		currentReceiveSequenceID = 0;
		
		if (this.replyHandler != null) {
			if (this.getReplyAddress().equals("")) {
				this.put(CommandScheme.REPLYADDRESS, this.genReplyAddress());
			}			
					
			this.replyListener = vertx.eventBus().consumer(this.getReplyAddress());
			log.debug("registry the command sender replyAddress:" + replyListener.address() + " for the command: " + this.getCommandAddress());
			
			if (options.getSendTimeout() >= 1) {
				timeoutID = vertx.setTimer(options.getSendTimeout(), timerID -> {
					ReplyException timeOutException = new ReplyException(ReplyFailure.TIMEOUT, "Timed out waiting for reply");
					sendExceptionReply(vertx.eventBus(), timeOutException, options);
				});
			}

			replyListener.handler(ar -> {				
				CommandResult result = CommandResult.fromMessageBody(ar.body());
				log.debug("receive progress: sequenceID=" + String.valueOf(result.getSequenceID()) + " delta=" + String.valueOf(result.getDelta()) + " progress=" + String.valueOf(result.getProgress()) + " total=" + String.valueOf(result.getTotal()));
				log.debug("currentReceiveSequenceID=" + String.valueOf(currentReceiveSequenceID) + " receiveBuffer.size=" + String.valueOf(receiveBuffer.size()));
				
				receiveBuffer.put(result.getSequenceID(), result);
				CommandResult lastSendedResult = null;
				while (receiveBuffer.containsKey(currentReceiveSequenceID)) {
					lastSendedResult = receiveBuffer.get(currentReceiveSequenceID);
					if (lastSendedResult.getProgress() != receiveProgress + lastSendedResult.getDelta()) {
						lastSendedResult.reBaseProgress(receiveProgress);
					}
					receiveProgress = lastSendedResult.getProgress();
					
					if (this.replyHandler != null) {
						log.debug("Reply to Sender progress: sequenceID=" + String.valueOf(lastSendedResult.getSequenceID()) + " delta=" + String.valueOf(lastSendedResult.getDelta()) + " progress=" + String.valueOf(lastSendedResult.getProgress()) + " total=" + String.valueOf(lastSendedResult.getTotal()));
						this.replyHandler.handle(lastSendedResult);												
					}
					
					receiveBuffer.remove(currentReceiveSequenceID);
					currentReceiveSequenceID ++;
				}
				if (lastSendedResult != null && lastSendedResult.isComplete()) {
					log.debug("unregistry the command sender replyAddress:" + replyListener.address() + " for the command: " + this.getCommandAddress());
					this.replyListener.unregister();
					if (timeoutID != -1) {
						vertx.cancelTimer(timeoutID);
						timeoutID = -1;
					}
				}
			});	
		}
		
//		MessageProducer<Object> sender = vertx.eventBus().sender(address, options);
//		sender.exceptionHandler(this::sendExceptionHandler);
//		sender.write(this);
		if (options.getSendTimeout() == -1) {
			options.setSendTimeout(DeliveryOptions.DEFAULT_TIMEOUT);
		}
		vertx.eventBus().send(address, this, options, ar ->{
			if (ar.failed()) {
				log.debug("send command to address:" + address + " failed, becuase: " + ar.cause().getMessage());
				sendExceptionReply(vertx.eventBus(), ar.cause(), options);
			}
		});
	}
	
	private void sendExceptionReply(EventBus ebus, Throwable cause, CommandDeliveryOptions options) {
		if (this.replyHandler != null) {
			CommandResult result = this.createResultObject();
			result.fail(cause);
			if (this.canMultiTimeReply()) {
				result.put(CommandScheme.SEQUENCEID, currentReceiveSequenceID);
				result.put(CommandScheme.TOTAL, receiveProgress + 1);
				result.put(CommandScheme.PROGRESS, receiveProgress + 1);
				result.put(CommandScheme.DELTA, 1);
			}
			
			result.put(CommandScheme.RESULT_REPLY_ON, getNow());
			options.setCodecName("CommandResult");		
			options = adjustToEventBusCurrentCodec(ebus, options);
			result.setDeliveryOptions(options);
			
			if (this.canMultiTimeReply()) {
				ebus.send(this.getReplyAddress(), result, options);	
			}
			else {
				this.replyHandler.handle(result);
			}
		}
		else {
			log.debug("Send Command to Consumer fail: " + cause.getMessage());
		}
	}
		
	private CommandResult endUpdateProgress(CommandResult result) {
		result.put(CommandScheme.SEQUENCEID, sendSequenceID ++);
		result.put(CommandScheme.TOTAL, (sendTotal == 0)? 1 : sendTotal);
		result.put(CommandScheme.PROGRESS, (sendTotal == 0)? 1 : sendTotal);
		result.put(CommandScheme.DELTA, ((sendTotal - sendProgress) <= 0)? 1 :  sendTotal - sendProgress);
		sendTotal = result.getTotal();
		sendProgress = result.getProgress();
		return result;
	}
	
	private void executeCommandWithSingleReply(Vertx vertx, String address, CommandDeliveryOptions options, Handler<CommandResult> replyHandler) {
		this.replyHandler = replyHandler;	
		if (this.replyHandler != null) {			
			vertx.eventBus().send(address, this, options, ar -> {
				CommandResult result;
				if (ar.succeeded()) {
					result = CommandResult.fromMessageBody(ar.result().body());
				}
				else {
					result = this.createResultObject();
					result.fail(ar.cause());
					log.error(ar.cause().toString());
				}				
				
				this.replyHandler.handle(result);			
			});		
		}
		else {
			vertx.eventBus().send(address, this, options);
		}
	}
	
	private String genReplyAddress() {
		return UUID.randomUUID().toString();		
	}
	
	private void createInvalidCommandWrap(String illegalCmdRaw) {		
		this.put(CommandScheme.ACCOUNT_ID, -1);
		this.put(CommandScheme.ADDRESS, CommandScheme.INVALID_COMMAND);
		this.put(CommandScheme.RAW_INVALID_COMMAND, illegalCmdRaw);	
	}
	
	private CommandDeliveryOptions adjustToEventBusCurrentCodec(EventBus ebus, CommandDeliveryOptions options) {
		try {
			Class<?> eventBusClz = ebus.getClass();
			Field userCodecMapField = eventBusClz.getDeclaredField("userCodecMap");
			userCodecMapField.setAccessible(true);
			ConcurrentMap<String, MessageCodec<?,?>> userCodecMap = (ConcurrentMap<String, MessageCodec<?,?>>)userCodecMapField.get(ebus);
			MessageCodec<?,?> cmdCodec = userCodecMap.get(options.getCodecName());			
			if (cmdCodec == null) {
				options.setCodecName(null);
			}
			userCodecMapField.setAccessible(false);			
		}
		catch (Exception e) {
			options.setCodecName(null);
		}
		return options;
	}
	
	private Vertx getVertxofEventBus(EventBus ebus) {
		try {
			Class<?> eventBusClz = ebus.getClass();
			Field vertxField = eventBusClz.getDeclaredField("vertx");
			vertxField.setAccessible(true);
			Vertx vertx = (Vertx)vertxField.get(ebus);
			vertxField.setAccessible(false);
			return vertx;	
		}
		catch (Exception e) {
			log.error("Can't explore the vertx of EventBus!");
			return null;
		}
	}
	
	private boolean setMessageCarrierReplyAddress(Message<?> msgCarrier, String address) {
		try {
			Class<?> messageClz = msgCarrier.getClass();
			Field replyAddress = messageClz.getDeclaredField("replyAddress");
			replyAddress.setAccessible(true);
			replyAddress.set(msgCarrier, address);
			replyAddress.setAccessible(false);
			return true;	
		}
		catch (Exception e) {
			log.error("Can't explore the Message of replyAddress field!");
			return false;
		}
	}
	
	private static Instant getNow() {
		return Instant.now();
	}
}
