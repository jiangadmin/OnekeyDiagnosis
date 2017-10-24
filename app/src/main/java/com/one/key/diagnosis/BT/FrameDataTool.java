package com.one.key.diagnosis.BT;

import com.frontier.qgdw3761.QGDW3761Frame;
import com.frontier.qgdw3761.QGDW3761Interface;
import com.frontier.qgdw3761.impl.QGDW3761Impl;
import com.frontier.util.ConvertDataUtil;

/**
 * 帧数据转换工具类
 * 
 * @author wqb
 * 
 */
public class FrameDataTool {
    public static final int BAUD_RATE_600 = 0x02;			//波特率600
	public static final int BAUD_RATE_2400 = 0x04;			//波特率2400
	public static final int BAUD_RATE_1200 = 0x03;			//波特率1200
	
	
	/**
	 * 将原始的数据格式转换成红外设备可识别的数据格式
	 * 
	 * @param byte[] beforedata 原始数据
	 * @param beforedata   波特率
	 * @return
	 */
	public static byte[] bufferTransition(byte[] beforedata,int baudRate) {
		byte[] data = null;
		try {
			if (beforedata == null || beforedata.length == 0) {
				return null;
			}
			data = new byte[beforedata.length + 6];
			data[0] = (byte) 0x86; // 数据开始标志
			data[1] = (byte) baudRate; // 波特率
			if (beforedata.length >= 128) {
				data[2] = 127;
				data[3] = (byte) (beforedata.length - 127);
			} else {
				data[2] = (byte) beforedata.length;
				data[3] = 0;
			}
			for (int i = 0; i < beforedata.length; i++) {
				data[i + 4] = beforedata[i];
			}
			byte cs = getCheckCode(data);
			data[data.length - 2] = cs;
			data[data.length - 1] = 0x61;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public static byte convert(int value) {
		if (value >= 128) {
			return (byte) (-1 * (256 - value));
		} else {
			return (byte) value;
		}
	}

	/**
	 * 获取校验码
	 * 
	 * @param data
	 *            数据区域
	 * @param data
	 *            帧类型
	 * 
	 * @return
	 */
	public static byte getCheckCode(byte[] data) {
		int type = 0;
		for (int i = 0; i < data.length - 2; i++) { //
			type = type + data[i];
		}

		return (byte) (type & 0xff);
	}

	/**
	 * 根据usb原始数据取得真实数据区域
	 * 
	 * @param beforeData
	 * @return
	 */
	public static byte[] getDataFromUsbData(byte[] beforeData) {
		int lowLength = beforeData[2]; // 数据区长度低八位
		int highLength = beforeData[3]; // 数据区长度高八位
		int length = lowLength + highLength;
		if (beforeData.length - length < 4) {
			return null;
		}
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = beforeData[i + 4];
		}

		return data;
	}

	/**
	 * 根据645格式的数据包装成376.1格式的数据
	 * 
	 * @param data
	 *            645格式的命令
	 * @param terAddress
	 *            终端地址
	 * @return 376.1格式的数据
	 */
	public static byte[] getTerminalDataFromDL645Data(String data,
			String terAddress) {

		QGDW3761Frame qFrame = new QGDW3761Frame();
		qFrame.setControlCode("4B");
		qFrame.setDevAddress(terAddress);
		qFrame.setAfn("10");
		qFrame.setSeq("71");
		qFrame.setDataUnitId("00 00 01 00 02 6B 82 32 10 00"
				.replaceAll(" ", ""));
		qFrame.setDataUnit(data.replace("FEFEFEFE", ""));
		qFrame.setPw("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
				.replaceAll(" ", ""));
		QGDW3761Interface qGDW3761Interface = new QGDW3761Impl();
		byte[] realData = qGDW3761Interface.packageFrame(qFrame);
		return realData;
	}
	 /**
     * 将十六进制字符串转成二进制字符串
     * @param str 十六进制
     * @param len 二进制的长度
     * @return
     */
	public static String toBin(String x, int len) {
		String re = "";
		StringBuffer result=new StringBuffer();
		int v = ConvertDataUtil.hexStringToByte(x)[0];
		do{       
			result.append(v%2);      
		    v/=2;      
		}
		while(v>0); 
		int i= 0;
		if(result.length()!=len){
			i=len-result.length();
			for(int z=0;z<i;z++){
				re +="0";
			}
			re +=result.reverse().toString();
		}else{
			re = result.reverse().toString();
		}
		
		return re;   
		
	  }
}
