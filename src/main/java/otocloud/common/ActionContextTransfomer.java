/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common;



import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class ActionContextTransfomer {
	
	public static void fromHttpHeaderToMessageHeader(HttpServerRequest request, DeliveryOptions options){		
		String accessToken = "";
		String account = "";
		String tAccount = "";
		String actor = "";
		
		try{
			accessToken = request.getHeader(ActionHttpContext.TOKEN_KEY);
		}catch(Exception e){
			accessToken = "";
		}
		try{
			account = request.getHeader(ActionHttpContext.ACCOUNT_KEY);
		}catch(Exception e){
			account = "";
		}
		try{
			tAccount = request.getHeader(ActionHttpContext.T_ACCOUNT_KEY);
		}catch(Exception e){
			tAccount = "";
		}
		try{
			actor = request.getHeader(ActionHttpContext.ACTOR_KEY);
		}catch(Exception e){
			actor = "";
		}
		

		if(accessToken != null)
			options.addHeader(AppMessageHeader.TOKEN_KEY, accessToken); 

		//账户信息切换
		if(tAccount != null && !tAccount.isEmpty()){
			options.addHeader(AppMessageHeader.ACCOUNT_KEY, tAccount);
			options.addHeader(AppMessageHeader.ACTOR_ACCOUNT_KEY, account); 
		}
			
		if(actor != null)
			options.addHeader(AppMessageHeader.ACTOR_KEY, actor); 
	}
	
	public static ActionHttpContext fromHttpRequestParamToMessageHeader(HttpServerRequest request){		
		String context = request.getParam(ActionHttpContext.CONTEXT_KEY);
		
		ActionHttpContext ret = new ActionHttpContext();		
		
		// contex中一定要有四个字段，格式：“{account}|{target_account}|{actor}|{access_token}”，如果没有值可以空着，如：“33||1310233999|”
		// TODO 如果context中没有四个参数直接返回，提示：传入参数不正确
		if (context != null
				&& context.split(ActionHttpContext.SPLIT_KEY, -1) != null
				&& context.split(ActionHttpContext.SPLIT_KEY, -1).length == 4) {

			String[] contexts = context.split(ActionHttpContext.SPLIT_KEY, -1);
			try {
				ret.setAccessToken(contexts[3]);
			} catch (Exception e) {
				ret.setAccessToken("");
			}
			try {
				ret.setAccount(contexts[0]);
			} catch (Exception e) {
				ret.setAccount("");
			}
			try {
				ret.setTargetAccount(contexts[1]);
			} catch (Exception e) {
				ret.setTargetAccount("");
			}
			try {
				ret.setActor(contexts[2]);
			} catch (Exception e) {
				ret.setActor("");
			}
			
			String tAccount = ret.getTargetAccount();			
			if(tAccount == null || tAccount.isEmpty()){
				ret.setTargetAccount(ret.getAccount());
			}		


		}
		return ret;
	}
	
	public static void setBusMessageHeader(ActionHttpContext httpContext, DeliveryOptions options){		

		options.addHeader(AppMessageHeader.TOKEN_KEY, httpContext.getAccessToken()); 		
		//账户信息切换
		if(httpContext.getTargetAccount() != null && !httpContext.getTargetAccount().isEmpty()){
			options.addHeader(AppMessageHeader.ACCOUNT_KEY, httpContext.getTargetAccount()); //本企业账户
			options.addHeader(AppMessageHeader.ACTOR_ACCOUNT_KEY, httpContext.getAccount()); //协作方操作员账户
		}
		options.addHeader(AppMessageHeader.ACTOR_KEY, httpContext.getActor()); 		

	}

	
	public static JsonObject fromMessageHeaderToActor(MultiMap headerMap){	
		JsonObject actorJsonObject = new JsonObject();
		
		//token
		if(headerMap.contains(AppMessageHeader.TOKEN_KEY)){
			actorJsonObject.put(AppMessageHeader.TOKEN_KEY, headerMap.get(AppMessageHeader.TOKEN_KEY));			
		};
		
		//操作员账户
		if(headerMap.contains(AppMessageHeader.ACTOR_ACCOUNT_KEY)){
			actorJsonObject.put(AppMessageHeader.ACTOR_ACCOUNT_KEY, headerMap.get(AppMessageHeader.ACTOR_ACCOUNT_KEY));			
		};
		
		//操作员
		if(headerMap.contains(AppMessageHeader.ACTOR_KEY)){
			actorJsonObject.put(AppMessageHeader.ACTOR_KEY, headerMap.get(AppMessageHeader.ACTOR_KEY));			
		};
			
		return actorJsonObject;
	}
	
}
