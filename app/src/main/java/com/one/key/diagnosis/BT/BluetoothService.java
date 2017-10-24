package com.one.key.diagnosis.BT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.frontier.util.ConvertDataUtil;
import com.one.key.diagnosis.activity.MainActivity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 
 * 该类用于设置和管理蓝牙连接。有三个线程，一个用于监听连接，一个用于连接其他设备，一个用于与其他设备传输数据
 */
public class BluetoothService {

	private static final String TAG = "BluetoothService";

	// 创建服务器socket时SDP的名字
	private static final String NAME = "BTPrinter";

	// private static final UUID MY_UUID =
	// UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 打印机 蓝牙串口服务

	// 成员变量
	private static BluetoothAdapter mAdapter;
	private static Handler mHandler;
	private static Handler readAmmeterHandler;
	private static AcceptThread mAcceptThread;
	private static ConnectThread mConnectThread;
//	private static ConnectedThread mConnectedThread;
	private static BTSocket btSocket;
	

	// 表示当前连接的状态
	public static final int STATE_NONE = 0; // 未连接

	private static int mState = STATE_NONE;
	public static final int STATE_LISTEN = 1; // 监听传入的连接
	public static final int STATE_CONNECTING = 2; // 初始化连接
	public static final int  STATE_CONNECTED = 3; // 与远程设备连接成功
	public static final int STATE_FAIL = -2; // 连接失败
	public static final int STATE_CONNECTEDED = -3; // 已连接
	public static final int STATE_CANCEL = 4; // 在设备选择列表中点击取消按钮
	public static final int STATE_RETURN_DATA = 5; // 返回二维码数据
	public static BluetoothService service = null;
	private Context mContext;
	private String address = null;
	private static BluetoothSocket socket = null;
	public static boolean isReturnDataToActivity = false;// 是否将扫描数据返回到Activity
	private static boolean REGISTER_FLAG = false;
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public static void setmHandler(Handler mHandler) {
		BluetoothService.mHandler = mHandler;
	}
	public boolean isReturnDataToActivity() {
		return isReturnDataToActivity;
	}

	public void setReturnDataToActivity(boolean isReturnDataToActivity) {
		this.isReturnDataToActivity = isReturnDataToActivity;
	}
	/**
	 * 准备一个新的打印机会话
	 * 
	 * @param context
	 *            UI Activity Context
	 * @param handler
	 *            用于发送消息到UI Activity
	 */
	public BluetoothService(Context context) {
		mContext = context;
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		REGISTER_FLAG = true;
		mContext.registerReceiver(broadcastReceiver, intentFilter);
	}
	
	/**
	 * 取消广播注册
	 */
	public void unRegisterReceiver(){
		try {
			mContext.unregisterReceiver(broadcastReceiver) ;
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
			
		
	}

	public static BluetoothService getInstance3(Context context, Handler handler) {
		if (service == null) {
			service = new BluetoothService(context);
		}
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		readAmmeterHandler = handler;
		return service;
	}
	public static BluetoothService getInstance(Context context, Handler handler) {
		if (service == null) {
			service = new BluetoothService(context);
		}
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		return service;
	}
	public static BluetoothService getInstance2(Context context, Handler handler) {
		if (service == null) {
			service = new BluetoothService(context);
		}
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		btSocket = BTSocket.getInstance();
		btSocket.setHandler(mHandler);
		return service;
	}
	/**
	 * 设置当前的连接状态
	 * 
	 * @param state
	 *            int 定义当前连接状态
	 */
	public static synchronized void setState(int state) {
		Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		// 把最新的状态传递给Handler，这样UI Activity能更新状态
		if(null != mHandler){
			Message message = Message.obtain();
			message.what = MainActivity.MESSAGE_STATE_CHANGE;
			message.arg1 = state;
			mHandler.sendMessage(message);
		}
	}

	/**
	 * 返回当前连接状态
	 */
	public static synchronized int getState() {
		return mState;
	}


	/**
	 * 启动服务。使AcceptThread开始处于监听模式下的会话。由Activity onResume()调用。
	 */
	public synchronized void start() {
		Log.d(TAG, "start");

		// 关闭试图建立链接的线程
//		if (mConnectThread != null) {
//			mConnectThread.cancel();
//			mConnectThread = null;
//		}

		// 关闭当前正在运行的线程
//		if (mConnectedThread != null) {
//			mConnectedThread.cancel();
//			mConnectedThread = null;
//		}
		if(null != btSocket){
			btSocket.stopConnectedThread();
		}

		// 启动线程监听BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * 启动ConnectThread发起一个连接到远程设备。
	 * 
	 * @param device
	 *            需要连接的设备
	 */
	private synchronized void connect(BluetoothDevice device) {
		Log.d(TAG, "connect to: " + device);

		// 关闭试图建立链接的线程
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// 关闭当前正在运行的线程
//		if (mConnectedThread != null) {
//			mConnectedThread.cancel();
//			mConnectedThread = null;
//		}
		if(null != btSocket){
			btSocket.stopConnectedThread();
		}

		// 启动与设备连接的线程
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
	}

	/**
	 * 启动ConnectedThread来管理蓝牙连接
	 * 
	 * @param socket
	 *            连接BluetoothSocket
	 * @param device
	 *            已连接的蓝牙设备
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.d(TAG, "connected");

		// 关闭已经连接的线程
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// 关闭当前正在运行的线程
//		if (mConnectedThread != null) {
//			mConnectedThread.cancel();
//			mConnectedThread = null;
//		}
		if(null != btSocket){
			btSocket.stopConnectedThread();
		}

		// 取消接收线程，因为我们只需要连接到一台设备
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// 启动线程来管理连接，并执行传输
		btSocket = BTSocket.getInstance();
		btSocket.setSocket(socket);
		btSocket.setHandler(mHandler);
//		mConnectedThread = new ConnectedThread(socket);
//		mConnectedThread.start();
		
		// 发送所连接设备的名称到UI Activity
		Message msg = mHandler
				.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * 与外设配对
	 */
	public synchronized void pair(BluetoothDevice device) {

		// 改变状态
		setState(STATE_CONNECTING);

		if (BluetoothDevice.BOND_NONE == device.getBondState()) {
			try {
				Method method = BluetoothDevice.class.getMethod("createBond");
				try {
					method.invoke(device);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else {
			connect(device);
		}

	}

	public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Log.d(TAG, "action=" + action);
			if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice bluetoothDevice = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (bluetoothDevice.getBondState()) {
				case BluetoothDevice.BOND_BONDING:
					Log.d(TAG, "正在配对...");
					break;
				case BluetoothDevice.BOND_BONDED:
					Log.d(TAG, "已配对...");
					//mContext.unregisterReceiver(broadcastReceiver);
					connect(bluetoothDevice);
					break;
				default:
					break;
				}
			}
		}
	};

	/**
	 * 停止所有线程
	 */
	public synchronized void stop() {
		Log.d(TAG, "停止所有线程");
		// setState(STATE_NONE);
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
//		if (mConnectedThread != null) {
//			mConnectedThread.cancel();
//			mConnectedThread = null;
//		}
		if(null != btSocket){
			btSocket.stopConnectedThread();
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
	}

	/**
	 * 写不同步
	 * 
	 * @param out
	 *            需要写入的字节数组
	 * @see ConnectedThread#write(byte[])
	 */
	public boolean write(byte[] out) {
		
//		// 创建临时对象
//		ConnectedThread r;
//		// 与ConnectedThread同步
//		if(mConnectedThread == null && getState() == STATE_CONNECTED){
//			mConnectedThread = new ConnectedThread(socket);
//			mConnectedThread.start();
//		}
//		synchronized (this) {
//			if (mState != STATE_CONNECTED) {
//				return false;
//			}
//			// 启动线程监听BluetoothServerSocket
//			if(mConnectedThread == null){
//				//mConnectedThread = new ConnectedThread(socket);
//				//mConnectedThread.start();
//			}
//			r = mConnectedThread;
//		}
//		// 写不同步
		return btSocket.write(out);
	}

	/**
	 * 连接失败，通知UI Activity.
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);

		// 发送失败消息到Activity
		Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.TOAST, "与设备连接失败");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * 连接断开时通知BTPrinterActivity界面
	 */
	private void connectionLost() {
		Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.TOAST, "设备连接断开");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * 该线程用于监听传入的连接，就像一个服务器端的客户端，他一直运行直到连接被接受或取消
	 */
	private class AcceptThread extends Thread {
		// 本地 server socket
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;

			// 创建一个新的服务器 socket
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			

			// 如果没有连接则一直监听
			while (mState != STATE_CONNECTED) {
				try {
					// 直到连接成功或出现异常
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}

				// 如果连接被接受
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// 启动连接线程
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// 尚未准备就绪或已连接 终止新的socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG,"Could not close unwanted socket",e);
							}
							break;
						}
					}
				}
				
			}
			
			Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			Log.d(TAG, "关闭 " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * 该线程用于连接远程设备，连接成功或失败
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			setAddress(device.getAddress()) ;

			// 从给定的BluetoothDevice获取BluetoothSocket
			try {
				// tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "create()失败", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// 总是关闭查找设备，因为查找设备会降低连接效率
			mAdapter.cancelDiscovery();

			try {
				// Log.d(TAG, "是否已连接:" + mmSocket.isConnected()) ;
				// 连接成功或是出现异常
				mmSocket.connect();
				Log.d(TAG, "连接成功");
			} catch (IOException e) {
				connectionFailed();
				Log.d(TAG, "连接失败");
				// 关闭socket
//				try {
//					mmSocket.close();
//				} catch (IOException e2) {
//					Log.e(TAG, "连接失败时调用close()方法", e2);
//				}
				BluetoothService.this.start();
				return;
			}

			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}
			
			// 启动connected线程
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

//	/**
//	 * 该线程用于与远程设备的连接 处理与远程设备的输入输出
//	 */
//	private class ConnectedThread extends Thread {
//		private final BluetoothSocket mmSocket;
//		private final InputStream mmInStream;
//		private final OutputStream mmOutStream;
//		private StringBuffer tmpScanData;
//
//		public ConnectedThread(BluetoothSocket socket) {
//			Log.d(TAG, "创建 ConnectedThread");
//			mmSocket = socket;
//			InputStream tmpIn = null;
//			OutputStream tmpOut = null;
//			// 得到BluetoothSocket的输入输出流
//			try {
//				tmpIn = socket.getInputStream();
//				tmpOut = socket.getOutputStream();
//			} catch (IOException e) {
//				Log.e(TAG, "temp sockets not created", e);
//			}
//
//			mmInStream = tmpIn;
//			mmOutStream = tmpOut;
//		}
//
//		public void run() {
//			Log.i(TAG, "BEGIN mConnectedThread");
//			byte[] buffer = new byte[1024];
//			int len;
//
//			byte[] temp = new byte[1024];
//			int tempLen = 0;
//			// 保持监听输入流
//			while (true) {
//				try {
//					// 从输入流中读取
//					len = mmInStream.read(buffer);
//					for (int i = 0; i < len; i++) {
//						if ((buffer[i] & 0xFF) != 0x61) { // 从设备返回的流中，整理出完整数据格式的数组
//							temp[tempLen] = buffer[i];
//							tempLen++;
//						} else {
//							temp[tempLen] = buffer[i];
//							tempLen++;
//							//if(ReadAmmeterPlugin.bTflag){
//								//handlerDeviceReturnData(temp, tempLen);
//							//}else{
//								// 将读到的数据交给handler统一处理
//								handlerDeviceReturnData(temp, tempLen,mHandler);
//							//}
//							tempLen = 0;
//							temp = new byte[1024];
//						}
//
//					}
//
//				} catch (IOException e) {
//					Log.e(TAG, "disconnected", e);
//					setState(STATE_NONE) ;
//					//通知页面改变连接状态
//					MainActivity.callPageJs("changeState(0)") ;
//					// connectionLost();
//					break;
//				}
//			}
//		}
//
//		/**
//		 * 连接时写入输出流
//		 * 
//		 * @param buffer
//		 *            需要写入的字节数组
//		 */
//		public boolean write(byte[] buffer) {
//			try {
//				mmOutStream.write(buffer);
//                // 发送消息到UI Activity
//				mHandler.obtainMessage(BTPrinterActivity.MESSAGE_WRITE, -1, -1,
//						buffer).sendToTarget();
//				return true;
//			} catch (IOException e) {
//				Log.e(TAG, "Exception during write", e);
//				return false;
//			}
//		}
//
//		public void cancel() {
//			try {
//				mmSocket.close();
//			} catch (IOException e) {
//				Log.e(TAG, "close() of connect socket failed", e);
//			}
//		}
//	}
//
//	private void handlerDeviceReturnData(byte[] data, int len,Handler mHandler) {
//    	  if (data == null || data.length < 7) { // 如果数据为空或者数据区域长度没有达到帧类型的那一位
//	 			return;
//	 		}
//	   	/***解析数据 **/ 
//	 		byte dataHead = data[0]; // 取出数据的第一个数字
//	 		if (dataHead != FrameDataTool.convert(0x86)) {
//	 			return;
//	 		}
//	 		int datatype = data[1];
//	 		int dataLen = data[2]<0?256-data[2]:data[2]; // 数据域长度
//	 		byte[] realData = new byte[dataLen]; // 定义真实的数据区域
//	 		for (int i = 4; i < dataLen + 4; i++) { // 将数据区域取出来
//	 			realData[i - 4] = data[i];
//	 		}
//	 	if (datatype == FrameDataTool.convert(0x81)) { // 扫描条码数据
//				StringBuffer sb = new StringBuffer("");
//				for (int i = 0; i < dataLen; i++) { // 将数据区域取出来
//					sb.append(realData[i] - 48); // 统一减去48后是条码的值
//				}
//				try {
//					if(isReturnDataToActivity)
//					{
//						
//	                    Message msg = new Message();
//	                    msg.what = BTPrinterActivity.MESSAGE_STATE_CHANGE;
//	                    msg.obj = sb.toString();
//	                    msg.arg1 =STATE_RETURN_DATA;
//	                    mHandler.sendMessage(msg);
//					}
//					else
//					{
//					  MainActivity.callPageJs("get_barcode('" + sb.toString() + "')"); // 通知页面
//					  ToolModule_Acitivity.callPageJs("get_barcode('" + sb.toString() + "')");
//					  // 将扫描到的条码传给handler
//					  if(readAmmeterHandler!=null){
//						  readAmmeterHandler.obtainMessage(1, 1, -1,sb.toString()).sendToTarget();
//					  }
//					}
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				
//			}
//	 		/** 解析表示数**/
//	 		else if(datatype == FrameDataTool.convert(0x83)){
//	 			try {
//	 				readAmmeterHandler.obtainMessage(2, 1, -1,realData).sendToTarget();
//	 		      
//	 			} catch (Exception e) {
//	 	 		}
//	 		}else if(datatype == -61){
//	 			if(ReadAmmeterPluginForJava.testFlag == -1){
//	 				readAmmeterHandler.obtainMessage(2, 1, 97,"97").sendToTarget();
//	 			}else{
//	 				if(ReadAmmeterPluginForJava.testType.equals("97")){
//	 					readAmmeterHandler.obtainMessage(2, 1, 97,"97").sendToTarget();
//	 				}else{
//	 					readAmmeterHandler.obtainMessage(2, 1, 7,"07").sendToTarget();
//	 				}
//	 			}
//	 		}else if(datatype == FrameDataTool.convert(0x89)){		//获取到按钮
//				int keyValue = data[4];
//				if(keyValue == FrameDataTool.convert(0x70)){			//红外按钮
//					MainActivity.callPageJs("prepare_readAmmeter()");
//					ToolModule_Acitivity.callPageJs("prepare_readAmmeter()");
//				}else if(keyValue == FrameDataTool.convert(0x0D)){			//OK按钮
//					MainActivity.callPageJs("ft_key_ok()");
//				}else if(keyValue == FrameDataTool.convert(0x26)){			//上按钮
//					MainActivity.callPageJs("ft_key_up()");
//				}else if(keyValue == FrameDataTool.convert(0x28)){			//下按钮
//					MainActivity.callPageJs("ft_key_down()");
//				}else if(keyValue == FrameDataTool.convert(0x25)){			//左按钮
//					MainActivity.callPageJs("ft_key_left()");
//				}else if(keyValue == FrameDataTool.convert(0x27)){			//右按钮
//					MainActivity.callPageJs("ft_key_right()");
//				}
//	 		}else if(datatype == FrameDataTool.convert(0x88)){ //获得终端电量信息
//	 			ReadAmmeterPlugin.powerStr = realData[0]+"%";
//			}
//	 		else{
//	 			
//	 		}
//		}

	
	
	/**
	 * *********************** 后期添加的方法 *****************************************
	 * @param data
	 * @param len
	

	private void handlerDeviceReturnData(byte[] data, int len) {
		if (data == null || data.length < 7) { // 如果数据为空或者数据区域长度没有达到帧类型的那一位
			return;
		}
		byte dataHead = data[0]; // 取出数据的第一个数字
		if (dataHead != FrameDataTool.convert(0x86)) {
			return;
		}
		int datatype = data[1];
		int dataLen = data[2]<0?256-data[2]:data[2]; // 数据域长度
		byte[] realData = new byte[dataLen]; // 定义真实的数据区域
		for (int i = 4; i < dataLen + 4; i++) { // 将数据区域取出来
			realData[i - 4] = data[i];
		}

		// 判断数据类型 条码cmd：0x81 询问包cmd:0x88 读取到电表示数cmd:0x83 读取电表示数失败cmd:0xC2

		if (datatype == FrameDataTool.convert(0x81)) { // 扫描条码数据
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < dataLen; i++) { // 将数据区域取出来
				sb.append(realData[i] - 48); // 统一减去48后是条码的值
			}
			try {
				MainActivity.callPageJs("get_barcode('" + sb.toString() + "')"); // 通知页面
			} catch (Exception e1) {
 				e1.printStackTrace();
			}
			
			try {
				if(MainActivity.isUni){
					UnifiedMoreActivity.scan(sb.toString());			//发送到统一视图的更多界面
				}
			
			} catch (Exception e) {
 				e.printStackTrace();
			}

		} else if(datatype == FrameDataTool.convert(0x88)){ //获得终端电量信息
  			//mHandler.obtainMessage(88, 1, 1,realData).sendToTarget();
 			ReadAmmeterPlugin.powerStr = realData[0]+"%";
		} else if (datatype == FrameDataTool.convert(0x83)) { // 读取到电表示数
			if (ReadAmmeterPlugin.terAddress == null) {

				QGDW3761Interface qGDW3761Interface = new QGDW3761Impl();
				QGDW3761Frame qFrame = qGDW3761Interface
						.unpackageFrame(realData);
				if (qFrame != null) {
					String terAddress = qFrame.getDevAddress();
					if (terAddress != null && !terAddress.trim().equals("")) {
						MainActivity.shouInfoByToast("得到终端地址： " + terAddress);

						ReadAmmeterPlugin.terAddress = terAddress;
					} else {
						ReadAmmeterPlugin.cmdControl
								.acceptAndDispatchResult(realData);
					}
				} else {
					ReadAmmeterPlugin.cmdControl
							.acceptAndDispatchResult(realData);
				}
			} else {
				ReadAmmeterPlugin.cmdControl.acceptAndDispatchResult(realData);

			}
		} else if (datatype == FrameDataTool.convert(0xC3)) { // 读取电表失败
			ReadAmmeterPlugin.cmdControl.siginError();
			//if(mHandler != null){
				mHandler.sendEmptyMessage(1);	//通知读取电表失败
			}//
		}else if(datatype == FrameDataTool.convert(0x89)){		//获取到按钮
			int keyValue = data[4];
			if(keyValue == FrameDataTool.convert(0x70)){			//红外按钮
				MainActivity.callPageJs("prepare_readAmmeter()");
			}else if(keyValue == FrameDataTool.convert(0x0D)){			//OK按钮
				MainActivity.callPageJs("ft_key_ok()");
			}else if(keyValue == FrameDataTool.convert(0x26)){			//上按钮
				MainActivity.callPageJs("ft_key_up()");
			}else if(keyValue == FrameDataTool.convert(0x28)){			//下按钮
				MainActivity.callPageJs("ft_key_down()");
			}else if(keyValue == FrameDataTool.convert(0x25)){			//左按钮
				MainActivity.callPageJs("ft_key_left()");
			}else if(keyValue == FrameDataTool.convert(0x27)){			//右按钮
				MainActivity.callPageJs("ft_key_right()");
			}
		}
	}
	*/

	/**
	 * 读取集抄终端的地址的方法
	 */
	public boolean readTerminalAddress() {
		String cmd = "68 32 00 32 00 68 49 FF FF FF FF 03 02 71 00 00 04 00 BF 16";
		byte[] data = ConvertDataUtil.hexStringToByte(cmd.replaceAll(" ", ""));
		byte[] temp = FrameDataTool.bufferTransition(data,
				FrameDataTool.BAUD_RATE_1200);
		return write(temp);
	}

	/**
	 * 开关灯
	 */
	public boolean switchLight() {
		return false;

	}

	/**
	 * 发送扫描条码指令的方法
	 */
	public boolean readBarcode() {
		byte[] ledBarCode = new byte[6];
		ledBarCode[0] = (byte) 0x86;
		ledBarCode[1] = (byte) 0x01;
		ledBarCode[2] = (byte) 0x00;
		ledBarCode[3] = (byte) 0x00;
		ledBarCode[4] = FrameDataTool.getCheckCode(ledBarCode);
		ledBarCode[5] = (byte) 0x61;
		return write(ledBarCode);

	}
}
