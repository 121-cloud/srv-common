/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.common;



import otocloud.common.util.ClassLoaderUtil;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class OtoCloudDirectoryHelper {
	
	public static String getConfigDirectory(){
		String homePath = System.getenv("OTOCLOUD_CONTAINER_HOME");
		if(homePath == null || homePath.isEmpty()){
			String filePath = "";
			try{
				filePath = ClassLoaderUtil.getExtendResource("../config/").getPath();
			}catch(Exception e){
				e.printStackTrace();
				filePath = "";
			}
			return filePath;
/*			File file=new File(filePath);
			if(file.exists()){
				return filePath;
			}*/
			
		}else{
			return homePath + "/config/";
		}

	}

	public static String getLibDirectory(){
		String homePath = System.getenv("OTOCLOUD_CONTAINER_HOME");
		if(homePath == null || homePath.isEmpty()){
			String filePath = "";
			try{
				filePath = ClassLoaderUtil.getExtendResource("../lib/").getPath();
			}catch(Exception e){
				e.printStackTrace();
				filePath = "";
			}
			return filePath;
/*			File file=new File(filePath);
			if(file.exists()){
				return filePath;
			}*/
			
		}else{
			return homePath + "/lib/";
		}

	}

}
