package com.one.key.diagnosis.BT;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BarCodeConvert {
	private static String hexString = "0123456789ABCDEF";

	private static final int START_PLACE = 0;

	public static final int CMD_PLACE = 1;

	private static final int LOW_LENGTH_PLACE = 2;

	private static final int HIGH_LENGTH_PLACE = 3;

	private static final int START_DATA_PLACE = 4;

	private static final int CS_PLACE = 4;

	private static final int END_PLACE = 5;

	public static String TXM_STR = "";
	
//	private static boolean test = true;
	
	
	public static String[]seneorDataTypeNames = new String[]{"超声波","高度","温度","湿度","紫外线","电场","红外"};

	/**
	 * 解析接收的手持机通信协议包
	 * @param successbytes
	 * @param handler
	 * @return
	 */
	public synchronized static String convertSuccessData(byte[] successbytes, updateTerminal terminal , String path) {
		if (null == successbytes || successbytes.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < successbytes.length; i++) {

		}
		String startHex = byteToHexString(successbytes[START_PLACE]);
		sb.append(startHex + " ");
		String cmdHex = byteToHexString(successbytes[CMD_PLACE]);
		sb.append(cmdHex + " ");
		String lowDataHex = byteToHexString(successbytes[LOW_LENGTH_PLACE]);
		sb.append(lowDataHex + " ");
		int lowDataLength = hex2Int(lowDataHex);
		String highDataDex = byteToHexString(successbytes[HIGH_LENGTH_PLACE]);
		sb.append(highDataDex + " ");
		int highDataLength = hex2Int(highDataDex);
		lowDataLength += highDataLength;
		byte[] dataByte = new byte[lowDataLength];
		for (int i = START_DATA_PLACE; i < lowDataLength + START_DATA_PLACE; i++) {
			dataByte[i - START_DATA_PLACE] = successbytes[i];
		}
		// 如果命令位 == 88 是信息版本数据包，详细解析
		if (cmdHex.equals("88")) {
			sb = new StringBuilder();
			sb.append(parseInfoData(dataByte) + " ");
			long currentVerLong = parseSoftVerDate(dataByte);
			long cardVerLong = updateUtil.cardFileVer(path);
			//测试用，每次都会升级
//			if(test){
//				currentVerLong = 1377878400000l;
//			}
			Log.e("22222222222222", currentVerLong + " :  dataByte= " + dataByte  + "   cardVerLong =  " +  cardVerLong);
			if(cardVerLong > currentVerLong){
				//只更新2014
				if(decode(byteToHexString(dataByte[5])).equals("4")){
					Log.e("111", "需要更新");
					terminal.update();
//					test = false;
				}else{
					BTSocket.where = "";
					BTSocket.bRun = false;
				}
//				new Thread(){
//					@Override
//					public void run() {
//						Message message = new Message();
//						message.what = 1;
//						handler.sendMessage(message);
//					}
//				}.start();
				
			}else if(cardVerLong <= currentVerLong){
				Log.e("111", "不需要更新");
//				new Thread(){
//					@Override
//					public void run() {
//						Message message = new Message();
//						message.what = 0;
//						handler.sendMessage(message);
//					}
//				}.start();
				BTSocket.where = "";
				BTSocket.bRun = false;
//				terminal.stopReadThread();
			}
		} else {
			// 如果命令位==81
			// 是扫描条形码数据包，解析出电表号字符串TXM_STR(条码数据的倒数第二位至倒数第11位,共计10位数)，留组红外通信包使用
			if (cmdHex.equals("81")) {// 05机器返回条形码命令位回复不确定，等07机器解决统一命令位81
				if (null != dataByte && dataByte.length > 0) {
					try {
						TXM_STR = decode(bytesToHexString(dataByte));
						TXM_STR = TXM_STR.substring(TXM_STR.length() - 1 - 10,
								TXM_STR.length() - 1);
					} catch (Exception e) {

					}
				}
			}
			// 其他数据包，粗略解析
			if (null != dataByte && dataByte.length > 0) {
				// String dataDec = decode(bytesToHexString(dataByte));
				String dataDec = bytesToHexString(dataByte);
				sb.append(dataDec + " ");
			}
		}
		String csHex = byteToHexString(successbytes[lowDataLength + CS_PLACE]);
		sb.append(csHex + " ");
		String endHex = byteToHexString(successbytes[lowDataLength + END_PLACE]);
		sb.append(endHex + " ");
		return sb.toString();
	}

	/**
	 * byte类型数组转16进制字符串
	 * 
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			stringBuilder.append(byteToHexString(src[i]));
		}
		return stringBuilder.toString();
	}

	/**
	 * byte类型字节转16进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byteToHexString(byte b) {
		StringBuilder stringBuilder = new StringBuilder("");
		int v = b & 0xFF;
		String hv = Integer.toHexString(v);
		hv = hv.toUpperCase();
		if (hv.length() < 2) {
			stringBuilder.append(0);
		}
		stringBuilder.append(hv);
		return stringBuilder.toString();
	}

	/**
	 * 16进制字符串转10进制字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// 将每2位16进制整数组装成一个字节
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}

	private static int hex2Int(String hexStr) {

		int i = Integer.parseInt(hexStr, 16); // the corresponding base 10
												// integer
		if (i >= 32768) {
			i -= 65536;
		}
		return i;
	}

	public static byte[] hexStringToByte(String hex) {
		hex = hex.toUpperCase();
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;

	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static final String bytesToHexString1(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param hexStr
	 *            16进制字符串"7A1C00120A"
	 * @param digit
	 *            从右边开始拆分的位数
	 * @return
	 */
	public static long toInt_b(String hexStr, int digit) {
		if (digit <= 0) {
			throw new IllegalArgumentException(
					"argument digit must be greater than zero!");
		}
		char[] chs = hexStr.toCharArray();
		long sum = 0;
		for (int i = chs.length - 1; i >= 0; i--) {
			sum += Character.digit(chs[i], 16)
					* (1L << ((chs.length - i - 1) % digit * 4));
		}
		return sum;
	}
	
	/**
	 * int转双字节hex
	 * @param b
	 * @return
	 */
	public static String int2DoubleHex(int b){
		StringBuilder sb = new StringBuilder();
		String hex = Integer.toHexString(b);
		if(hex.length()==1){
			sb.append("000").append(hex);
		}else if(hex.length()==2){
			sb.append("00").append(hex);
		}else if(hex.length()==3){
			sb.append("0").append(hex);
		}else if(hex.length()==4){
			sb.append(hex);
		}else{
			
		}
		return sb.toString();
	}
	
	/**
	 * int转单字节hex
	 * @param b
	 * @return
	 */
	public static String int2SingleHex(int b){
		StringBuilder sb = new StringBuilder();
		String hex = Integer.toHexString(b);
		if(hex.length()==1){
			sb.append("0").append(hex);
		}else if(hex.length()==2){
			sb.append(hex);
		}else{
			
		}
		return sb.toString();
	}
	
	/**
	 * 二位字节16进制高低位交换
	 * @param hexStr
	 * @return
	 */
	public static String doubleHexLowHighConvert(String hexStr){
		StringBuilder sb = new StringBuilder();
		for(int i=hexStr.length();i>0;i=i-2){
			sb.append(hexStr.substring(i-2,i));
		}
		return sb.toString();
	}
	
	
	
	/**
	 * 解析软件版本日期转换为long类型数值
	 * @param byteData
	 * @return
	 */
	public static long parseSoftVerDate(byte[] byteData){
		StringBuilder sb = new StringBuilder("");
		if (null == byteData || byteData.length != 9) {
			return -1L;
		}else{
			//年
			String softY = "201"+new String(new byte[]{byteData[5]});
			//月
			String softM = new String(new byte[]{byteData[6]});
			softM = String.valueOf(toInt_b(softM, softM.length()));
			//日
			String softD = new String(new byte[]{byteData[7],byteData[8]});
		    sb.append(softY).append("-").append(softM).append("-").append(softD);
		    Date d = null;
			try {
				d = new SimpleDateFormat("yyyy-MM-dd").parse(sb.toString());
			} catch (ParseException e) {
				d = new Date();
				e.printStackTrace();
			}
		    return d.getTime(); 
		}
	}

	public static String parseInfoData(byte[] byteData) {
		StringBuilder sb = new StringBuilder("");
		if (null == byteData || byteData.length != 9) {
			sb.append("信息数据格式不正确:").append(bytesToHexString(byteData))
					.append(" ");
		} else {
			try {
				String batHex = byteToHexString(byteData[0]);
				//sb.append("Bat : ").append(toInt_b(batHex,batHex.length()))
				//		.append("% ");
				String hardY = byteToHexString(byteData[1]);
				//sb.append("(H)ver : 201").append(decode(hardY)).append("年");
				String hardM = byteToHexString(byteData[2]);
				String hexHardM = decode(hardM);
				//sb.append(String.valueOf(toInt_b(hexHardM,hexHardM.length()))).append("月");
				String hardD = bytesToHexString(new byte[]{byteData[3],byteData[4]});
				//sb.append(decode(hardD)).append("日 ");

				String softY = byteToHexString(byteData[5]);
				sb.append("检测手持机软件版本 : 201").append(decode(softY)).append("年");
				String softM = byteToHexString(byteData[6]);
				String hexSoftM = decode(softM);
				sb.append(String.valueOf(toInt_b(hexSoftM,hexSoftM.length()))).append("月");
				String softD = bytesToHexString(new byte[]{byteData[7],byteData[8]});
				sb.append(decode(softD)).append("日 ");
			} catch (Exception e) {
				sb = new StringBuilder("");
				sb.append("信息数据解析失败:").append(bytesToHexString(byteData))
						.append(" ");
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		//String hex = "FFFFFC18";
		//System.out.println(Double.parseDouble(String.valueOf(toInt_b(hex,8)))/1000);
		System.out.println(byteToHexString((byte)(97)));
	}
	
}
