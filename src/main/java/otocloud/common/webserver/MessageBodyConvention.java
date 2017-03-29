package otocloud.common.webserver;

/**
 * 消息体约定。
 * 用于标记"通过WebServer转发后，HTTP请求的数据在事件总线消息体中的字段"。
 * <p/>
 * Created by zhangye on 2015-10-28.
 */
public class MessageBodyConvention {
    /**
     * 包含应用注册 REST API 时提供的注册信息( Uri 和 Method )。
     */
    public static final String API = "api";

    /**
     * 应用所注册的 REST API 的URL地址。
     * 获取方式如下：
     * <pre>
     *  //msg is a {@code JsonObject}.
     *  JsonObject api = msg.getJsonObject({@linkplain MessageBodyConvention#API});
     *  String uri = api.getString({@linkplain MessageBodyConvention#API_URI});
     * </pre>
     */
    public static final String API_URI = "uri";

    /**
     * 应用所注册的 REST API 的 {@linkplain io.vertx.core.http.HttpMethod HttpMethod}
     */
    public static final String API_METHOD = "method";

    /**
     * 包含HTTP请求时的路径参数和查询参数。
     * 例如, 如果请求的URL为"http://.../app/:id?num=1"，
     * 那么该字段对应的JsonObject中包含"id"(路径参数)和"num"(查询参数)两个Key。
     */
    public static final String HTTP_QUERY = "queryParams";

    /**
     * HTTP请求时的原始URL地址，该地址是用户输入的完整地址。
     * 例如 {@code http://localhost:8080/api/app/fun/1?para1=0}。
     */
    public static final String HTTP_ABS_URI = "absUri";

    /**
     * HTTP请求时原始的URL地址，该地址只包含完整地址({@linkplain MessageBodyConvention#HTTP_ABS_URI})中端口号以后的部分。
     * 例如 {@code /api/app/fun/1?para1=0}。
     */
    public static final String HTTP_URI = "uri";

    /**
     * HTTP请求时原始的URL地址，该地址只包含{@linkplain MessageBodyConvention#HTTP_URI}中去掉查询参数的部分。
     * 例如 {@code /api/app/fun/1}
     */
    public static final String HTTP_PATH = "path";

    /**
     * 包含HTTP请求体。
     * 当发送POST/PUT请求时，HTTP请求体的内容将映射到该字段内。
     */
    public static final String HTTP_BODY = "content";

    /**
     * 登录用户产生的会话，在事件总线消息体中表现为一个{@linkplain io.vertx.core.json.JsonObject}。
     * 当用户发起登录请求时，WebServer会生成与该用户对应的会话({@code Session})。
     * 在后续的HTTP请求中，同一个用户的会话将在不同应用之间传递。
     */
    //public static final String SESSION = "session";

    /**
     * 当前登录用户的企业账户ID。
     */
    public static final String SESSION_ACCT_ID = "acct_id";

    /**
     * 当前登录用户的ID。
     */
    public static final String SESSION_USER_ID = "user_id";
    
    public static final String SESSION_USER_NAME = "user_name";

}
