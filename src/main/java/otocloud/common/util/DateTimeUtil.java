/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common.util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class DateTimeUtil {
	public static String now(String formatStr) {		
		Date date=new Date(); 
		DateFormat format=new SimpleDateFormat(formatStr); 
		return format.format(date); 
	}
}
