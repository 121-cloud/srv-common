package otocloud.common;

public class CommandScheme {
	public static final String FROM_SERVICE_NAME = "fromServiceName";
	public static final String ACCOUNT_ID = "accountID";
	public static final String SENDER_ID = "senderID";	
	public static final String SENDER_LOC = "senderloc";
	public static final String ADDRESS = "address";
	public static final String GATEWAY_ADDRESS = "gatewayAddress";
	public static final String REPLYADDRESS = "replyAddress";
	public static final String ACTION = "action";
	public static final String PARAMS = "queryParams";
	public static final String CONTENT = "content";
	public static final String SESSION = "session";
	public static final String SESSION_ID = "sessionID";
	public static final String REST_API = "restAPI";
	public static final String ABS_URI = "absUri";
	public static final String DELIVERYOPTIONS = "deliveryOptions";
	public static final String MULTITIMEREPLY = "multiTimeReply";
	
	public static final String COMMAND = "command";
	public static final String INVALID_COMMAND = "unkown";	
	public static final String COMMAND_START_ON = "startOn";	
	public static final String PROGRESS = "progress";
	public static final String TOTAL = "total";
	public static final String DELTA = "delta";
	public static final String SEQUENCEID = "sequenceID";
	public static final String RESULT_DATA = "data";
	public static final String RESULT_STATUS_CODE = "statusCode";
	public static final String RESULT_STATUS_MESSAGE = "statusMessage";
	public static final String RESULT_STACK_TRACE = "stackTrace";
	
	public static final String RESULT_REPLY_ON = "replyOn";
	public static final String RAW_INVALID_COMMAND = "rawInvalidCommand";
	public static final String RAW_INVALID_RESULT = "rawInvalidResult";
		
	public static final String GATEWAY_ADDRESS_PREFIX = "otocloud-gw-";
	
	public static String getGatewayAddress(String accountid) {
		return GATEWAY_ADDRESS_PREFIX + accountid;
	}
}
