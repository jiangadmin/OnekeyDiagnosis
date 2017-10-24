package com.one.key.diagnosis.BT;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class updateTerminal {
	private static final String LOG_TAG = "updateTerminal";
	private BTSocket btSocket;
	private BluetoothSocket _socket;
	private updateUtil util;
	private static int loopFlag = 0;
	private static int pkgNum = 0;
	private String smsg = "";
	boolean bRun = true;
//	Handler handler = null;
	Thread readThread = null;
	private InputStream is;
	private String path;
	private boolean isRead = true;

	public updateTerminal() {
//		handler = new cls2();
//		readThread = new cls1();
//		util = new updateUtil();
		btSocket = BTSocket.getInstance();
	}

	/**
	 * 发送获取版本号命令(更新文件已经存在)
	 */
	public void getTerminalVersions(String path) {
		BTSocket.where = BTSocket.WHERE_FROM_UPDATETERMINAL;
		pkgNum = 0;
		this.path = path;
		isRead = true;
//		readThread = new cls1();
//		readThread.start();
		util = new updateUtil();
		Log.e(LOG_TAG, "获取版本号");
//		if (null == _socket) {
//			Log.e(LOG_TAG, "蓝牙未连接");
//			return;
//		}
		btSocket.write(BarCodeConvert.hexStringToByte(util.getTerminalVersions()));
	}  

	public void setSocket(BluetoothSocket _socket) {
		this._socket = _socket;
		if (null == _socket) {
			Log.e(LOG_TAG, "蓝牙未连接");
			return;
		}
		try {
			is = _socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public void setSocket() {
//		this._socket = btSocket.getSocket();
//		if (null == _socket) {
//			Log.e(LOG_TAG, "蓝牙未连接");
//			return;
//		}
//		try {
//			is = _socket.getInputStream();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * 发送获取版本号命令
	 */
	public void getTerminalVersions() {
//		if (null == _socket) {
//			Log.e(LOG_TAG, "蓝牙未连接");
//			return;
//		}
		btSocket.write(BarCodeConvert.hexStringToByte(util.getTerminalVersions()));
	}

	/**
	 * 更新程序
	 */
	public void update() {
		new Thread() {
			@Override
			public void run() {
				Log.e(LOG_TAG, "下载更新");
				// 读取更新文件
				List<byte[]> filedataList = util.readCardFile(path);
				Log.i(LOG_TAG, "写入手持机用户区块");
				// 组包写入到用户区block
				if (null != filedataList && filedataList.size() > 0) {
					String start = "86130402";
					String end = "61";
					String totalBlock = BarCodeConvert
							.int2DoubleHex(filedataList.size() - 1);
					totalBlock = BarCodeConvert
							.doubleHexLowHighConvert(totalBlock);
					for (int i = 0; i < filedataList.size() - 1; i++) {
						if(loopFlag >= 5){
							loopFlag = 0;
							return;
						}
						StringBuilder sb = new StringBuilder();
						String block = BarCodeConvert.int2DoubleHex(i);
						block = BarCodeConvert.doubleHexLowHighConvert(block);
						byte[] data = filedataList.get(i);
						String dataHex = BarCodeConvert.bytesToHexString(data);
						sb.append(start).append(block).append(totalBlock)
								.append(dataHex);
						long dataCe = BarCodeConvert.toInt_b(sb.toString(), 2);
						int dataCeInt = (int) dataCe % 256;
						String dataCeStr = BarCodeConvert
								.int2SingleHex(dataCeInt);
						sb.append(dataCeStr).append(end);
						loopSend(sb.toString());
					}
					Log.i(LOG_TAG, "发送手持机升级程序指令");
					StringBuilder sb = new StringBuilder();
					String updateStart = "86140800";
					byte[] last = filedataList.get(filedataList.size() - 1);
					String hexDataLength = BarCodeConvert
							.bytesToHexString(new byte[] { last[0], last[1],
									last[2], last[3] });
					String hexDataCheck = BarCodeConvert
							.bytesToHexString(new byte[] { last[4], last[5],
									last[6], last[7] });
					sb.append(updateStart).append(hexDataLength)
							.append(hexDataCheck);
					long dataCe = BarCodeConvert.toInt_b(sb.toString(), 2);
					int dataCeInt = (int) dataCe % 256;
					String dataCeStr = BarCodeConvert.int2SingleHex(dataCeInt);
					sb.append(dataCeStr).append("61");
					// loop发送，5次未发送成功，停止更新
					loopSend(sb.toString());
					pkgNum = 0;
					
					Log.e(LOG_TAG, "下载结束");
					BTSocket.where = "";
					BTSocket.bRun = false;
//					stopReadThread();
				}
			}
		}.start();
	}
	
	public void stopReadThread(){
		if(null != readThread){
			isRead = false;
			readThread.interrupt();
			readThread.stop();
		}
	}

	private void loopSend(String sendStr) {
		BTSocket.where = BTSocket.WHERE_FROM_UPDATETERMINAL;
		btSocket.write(BarCodeConvert.hexStringToByte(sendStr));
		try {
			Thread.sleep(900 + loopFlag * 50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (smsg.trim().equals(updateUtil.CMD_13_SUCCESS)
				|| smsg.trim().equals(updateUtil.CMD_14_SUCCESS)) {
			loopFlag = 0;
			pkgNum++;
			Log.e(LOG_TAG, "第 " + String.valueOf(pkgNum) + " 包");
			Log.e(LOG_TAG, "写入：" + sendStr);
			Log.e(LOG_TAG, "成功：" + smsg);
			smsg = "";
			// 发送当前更新消息
		} else {
			if ((loopFlag++) < 5) {
				Log.i(LOG_TAG, "第" + loopFlag + "次写入用户区");
				loopSend(sendStr);
			}
			Log.w(LOG_TAG, "写入用户区失败");
		}
	}

	/**
	 * 接收线程
	 * 
	 * @author Administrator
	 * 
	 */
	private class cls1 extends Thread {
		public void run() {
			Log.e(LOG_TAG, "启动线程接受消息");
			byte abyte0[];
			bRun = true;
			while (isRead) {
				Log.e(LOG_TAG, "接受消息");
				int j = 0;
				int k = 0;
				int n = 0;
				try {
					do {
						if (is.available() != 0) {
							abyte0 = new byte[100];
							do {
								int i = is.read();
								abyte0[j] = (byte) i;
								if (i == 97) {
									if (is.available() != 0) {
										n = is.read();
										if (n == 0) {
											break;
										} else {
											j++;
											abyte0[j] = (byte) n;
										}
									} else {
										break;
									}
								}
								j++;
							} while (true);
							j = 0;
							n = 0;
							String msg = BarCodeConvert.convertSuccessData(
									abyte0, updateTerminal.this, path);
							Log.e(LOG_TAG, "返回消息：" + msg);
//							String msg = BarCodeConvert.convertSuccessData(
//									abyte0, handler, path);
							smsg = msg;
						}
					} while (bRun);
				} catch (IOException localIOException) {
				}

			}

		}

	}
	
	public void convertMsg(byte[] abyte0){
		String msg = BarCodeConvert.convertSuccessData(
				abyte0, updateTerminal.this, path);
		Log.e(LOG_TAG, "返回消息：" + msg);
//		String msg = BarCodeConvert.convertSuccessData(
//				abyte0, handler, path);
		smsg = msg;
	}

	/**
	 * 发送指令
	 * 
	 * @param cmd
	 */
	private void sendCmdData(String cmd) {
		OutputStream localOutputStream = null;
		int i = 0;
		try {
			// || !_socket.isConnected()
			if (null == _socket) {
				Log.e("蓝牙通信", "未建立蓝牙连接");
				return;
			}
			localOutputStream = _socket.getOutputStream();
			byte[] arrayOfByte1 = BarCodeConvert.hexStringToByte(cmd);
			if (i < arrayOfByte1.length) {
				localOutputStream.write(arrayOfByte1);
			}
		} catch (IOException localIOException) {
			return;
		}
	}

//	/**
//	 * 消息管理Handler
//	 * 
//	 * @author Administrator
//	 * 
//	 */
//	Handler handler = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case 4: // 开启下载更新
//				break;
//			case 3: // 下载更新结束
//				bRun = false;
//				readThread.stop();
//				break;
//			case 1:// 提示有可更新升级文件
//				Log.e(LOG_TAG, "服务端有更新版本软件");
//				// 目前没和服务器关联，所以直接更新
//				update();
//				break;
//			case 0:// 提示当前手持机软件版本已为最新
//				Log.e(LOG_TAG, "手持机已是最新版本");
//				readThread.stop();
//				break;
//			}
//		}
//	};

}
