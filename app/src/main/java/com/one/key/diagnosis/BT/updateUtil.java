package com.one.key.diagnosis.BT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class updateUtil {
	//获取版本号命令
	public static final String OPEN_INFO = "860800008E61";
	public static final String CMD_13_SUCCESS = "86 93 00 00 19 61";
    public static final String CMD_14_SUCCESS = "86 94 00 00 1A 61";
	
	/**
	 * 获取小终端版本号（命令）
	 */
	public String getTerminalVersions(){
		return OPEN_INFO;
	}
	
	/**
	 * 读取更新文件
	 * @return
	 */
	public List<byte []> readCardFile(String path){
		File file = new File(path);
		List<byte[]> blockList = new ArrayList<byte[]>();
		if(file.exists()){
			FileInputStream fis = null;
		    try {
		    	byte [] block;
				fis = new FileInputStream(file);
			    while(fis.available()!=0){
			    	block = new byte [512];
			    	fis.read(block);
			    	if(null !=block){
			    		blockList.add(block);
			    	}
			    }
			    //blockList.add(ymdBytes);
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if(null!=fis){
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return blockList;
	}
	
	/**
	 * 检查手机卡升级文件版本
	 * @return
	 */
	public static long cardFileVer(String path){
		File file = new File(path);
		if(!file.exists()){
			return -1;
		}
		String fileName = file.getName();
		String dateStr = fileName.substring(fileName.indexOf("_")+1,fileName.indexOf(".gjf"));
		long dateLong = -1;
		try {
			Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
			dateLong = d.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			//return -1;
		}
		return dateLong;
	}
}
