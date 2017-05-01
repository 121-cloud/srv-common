package otocloud.common;

/**
 * 约定了事件总线消息体中Session内的字段名称。
 *
 * Created by lj on 2016-10-27.
 */
public class SessionSchema {

    public static final String CURRENT_USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String ORG_ACCT_ID = "acct_id";
    
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRES_IN = "[EXPIRE]"; //失效时间
    
/*    public static final String BIZ_UNIT_ID = "biz_unit_id";
    public static final String IS_GLOBAL_BU = "is_global_bu";  //是否全局业务单元
    public static final String BIZ_UNIT_POST_ID = "biz_unit_post_id";  //岗位
    public static final String APP_ACTIVITY_ID = "app_activity_id";  //当前活动
*/
}
