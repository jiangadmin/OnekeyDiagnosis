package com.one.key.diagnosis.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 管理系统的配置信息
 * @author wqb
 *
 */
public class ConfigTool {
	private final static String PREFERENCES_NAME = "config";			//配置文件名称
	private static SharedPreferences preferences;
	private final static String SERVER_URL = "server_url";			//服务器地址key
	private final static String USER_NAME = "user_name";			//服务器用户名key
	private final static String PASSWORD = "password";				//服务器密码key
	private final static String IMG_UPLOAD = "img_upload";			//是否上传图片key
	private final static String TASK_INFO_UPLOAD = "task_info_upload";//是否上传工单信息key
	private final static String GPS_UPLOAD_TIME = "gps_upload_time";	//gps上传时间间隔
	private final static String BL_MAC = "bl_mac";						//连接蓝牙设备的mac地址
	private final static String PRINTER_BL_MAC = "printer_bl_mac";						//连接蓝牙打印机设备的mac地址
	private final static String GPS_UPLOAD_ABLE = "gps_upload_able";			//gps是否获取及上传
	private final static String APN_FLAG = "1";			//当前使用的第几APN
	
	/**
	 * 使用第几个apn
	 * @param context
	 * @param flag (1:移动，2：联通，3：其他)
	 */
	public static void setApnFlag(Context context, String flag){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putString(APN_FLAG, flag);
		editor.commit();
	}
	
	/**
	 * 修改gps是否获取及上传
	 * @param context
	 * @param able	是否上传
	 */
	public static void setGpsUploadAble(Context context, boolean able){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putBoolean(GPS_UPLOAD_ABLE, able);
		editor.commit();
	}
	
	
	/**
	 * 修改连接蓝牙打印设备的mac地址
	 * @param context
	 * @param BlMac	连接蓝牙设备的mac地址
	 */
	public static void setPrinterBlMac(Context context, String printerBlMac){
		if(printerBlMac == null || printerBlMac.trim().equals("")){
			return;
		}
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putString(PRINTER_BL_MAC, printerBlMac);
		editor.commit();
	}
	
	/**
	 * 修改连接蓝牙设备的mac地址
	 * @param context
	 * @param BlMac	连接蓝牙设备的mac地址
	 */
	public static void setBlMac(Context context, String BlMac){
		if(BlMac == null || BlMac.trim().equals("")){
			return;
		}
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putString(BL_MAC, BlMac);
		editor.commit();
	}
	
	/**
	 * 设置服务器地址
	 * @param context
	 * @param serverUrl		新的服务器地址
	 */
	public static void setServerPath(Context context, String serverUrl){
		if(serverUrl == null || serverUrl.trim().equals("")){
			return;
		}
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putString(SERVER_URL, serverUrl);
		editor.commit();
	}
	
	
	/**
	 * 修改服务器用户名
	 * @param context
	 * @param userName	新的服务器用户名
	 */
	public static void setUserName(Context context, String userName){
		if(userName == null || userName.trim().equals("")){
			return;
		}
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putString(USER_NAME, userName);
		editor.commit();
	}
	
	/**
	 * 修改服务器密码
	 * @param context
	 * @param password	新的服务器密码
	 */
	public static void setPassword(Context context, String password){
		if(password == null || password.trim().equals("")){
			return;
		}
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putString(PASSWORD, password);
		editor.commit();
	}
	
	/**
	 * 修改图片是否上传标记
	 * @param context
	 * @param isUpload	是否上传
	 */
	public static void setImgUpload(Context context, boolean isUpload){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putBoolean(IMG_UPLOAD, isUpload);
		editor.commit();
	}
	
	/**
	 * 修改工单信息是否上传标记
	 * @param context
	 * @param isUpload
	 */
	public static void setTaskInfoUpload(Context context, boolean isUpload){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putBoolean(TASK_INFO_UPLOAD, isUpload);
		editor.commit();
	}
	
	
	/**
	 * 修改gps上传时间间隔  单位为分钟
	 * @param context
	 * @param time		时间间隔
	 */
	public static void setGpsUploadTime(Context context, int time){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_WRITEABLE);
		Editor editor = preferences.edit();
		editor.putInt(GPS_UPLOAD_TIME, time);
		editor.commit();
	}
	
	
	
	/**
	 * 得到营销系统地址
	 * @param context	上下文
	 * @return
	 */
	public static String getServerUrl(Context context){
		return getServerPath(context)+"/ftf/mobileInterface/terminalData.action";
	 
	}
	
	/**
	 * 得到服务器地址
	 * @param context	上下文
	 * @return
	 */
	public static String getServerPath(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getString(SERVER_URL, "http://192.168.80.233:8188");
//		return	preferences.getString(SERVER_URL, "http://10.101.1.185:5100");
	}
	/**
	 * 得到服务器用户名
	 *   @param context
	 * @return
	 */
	public static String getUserName(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getString(USER_NAME, "");
	}
	
	/**
	 * 得到服务器密码
	 * @param context
	 * @return
	 */
	public static String getPassword(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getString(PASSWORD, "");
	}
	
	/**
	 * 得到图片是否上传
	 * @param context
	 * @return
	 */
	public static boolean getImgUpload(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getBoolean(IMG_UPLOAD, false);
	}
	
	/**
	 * 得到工单信息是否上传
	 * @param context
	 * @return
	 */
	public static boolean getTaskInfoUpload(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getBoolean(TASK_INFO_UPLOAD, true);
	}
	/**
	 * 得到gps上传时间间隔,单位是分钟
	 * @param context
	 * @return
	 */
	public static int getGpsUploadTime(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		int time =	preferences.getInt(GPS_UPLOAD_TIME, 5);
		if(time <= 0){
			return 5;
		}
		return time;
	}
	
	/**
	 * 得到连接蓝牙设备的mac地址
	 * @param context
	 * @return
	 */
	public static String getBlMac(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getString(BL_MAC, "");
	}
	
	/**
	 * 得到连接蓝牙设备的mac地址
	 * @param context
	 * @return
	 */
	public static String getPrinterBlMac(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getString(PRINTER_BL_MAC, "");
	}
	
	/**
	 * 得到gps是否获取及上传
	 * @param context
	 * @return
	 */
	public static boolean getGpsUploadAble(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getBoolean(GPS_UPLOAD_ABLE, true);
	}
	
	/**
	 * 得到gps是否获取及上传
	 * @param context
	 * @return
	 */
	public static String getApnFlag(Context context){
		preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return	preferences.getString(APN_FLAG, "1");
	}
}
