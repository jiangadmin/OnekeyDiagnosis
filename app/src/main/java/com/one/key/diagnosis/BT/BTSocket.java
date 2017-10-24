package com.one.key.diagnosis.BT;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.one.key.diagnosis.activity.MainActivity;
import com.one.key.diagnosis.utils.MyHandle;
import com.one.key.diagnosis.view.ConfirmDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class BTSocket {
    /**
     * 扫描条形码按键
     */
    public static final int AMMETER_SCANNING = 0;
    /**
     * 红外按键
     */
    public static final int AMMETER_INFRARED = 1;
    /**
     * OK按键
     */
    public static final int AMMETER_OK = 2;
    /**
     * 向上按钮
     */
    public static final int AMMETER_UP = 3;
    /**
     * 向下按钮
     */
    public static final int AMMETER_DOWN = 4;
    /**
     * 向左按钮
     */
    public static final int AMMETER_LEFT = 5;
    /**
     * 向右按钮
     */
    public static final int AMMETER_RIGHT = 6;
    /**
     * 返回数据
     */
    public static final int AMMETER_BACK_DATA = 7;
    /**
     * 异常
     */
    public static final int AMMETER_ERROR = 8;
    private BluetoothSocket socket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler handler;
    private static ConnectedThread mConnectedThread;
    private Handler readAmmeterHandler;
    private Handler readConcentratorHandler;
    public static final String WHERE_FROM_UPDATETERMINAL = "where_from_updateterminal";
    public static String where = "";
    private updateTerminal terminal;
    public static boolean bRun = true;

    public static BTSocket instance;
    private byte[] temp;
    private int tempLen;
    private Timer timer;
    private TimerTask task;

    private final String TAG = "BTSocket";

    private Context context;

    private BTSocket() {
    }

    /**
     * 单态
     */
    public static BTSocket getInstance() {
        where = "";
        if (instance == null) {
            instance = new BTSocket();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void setSocket(BluetoothSocket socket) {
        if (this.socket != socket) {

            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread.setFlag();
                mConnectedThread = null;
            }
            if (null != socket) {
                this.socket = socket;

                try {
                    mmInStream = socket.getInputStream();
                    mmOutStream = socket.getOutputStream();
                    mConnectedThread = new ConnectedThread();
                    mConnectedThread.start();

                    terminal = new updateTerminal();
                    where = WHERE_FROM_UPDATETERMINAL;
                    terminal.getTerminalVersions(Environment.getExternalStorageDirectory().getPath() + "/upgrade_2014-12-22.gjf");
//					terminal.getTerminalVersions("file:///android_assets/upgrade_2014-12-22.gjf");
                } catch (IOException e) {
                    e.printStackTrace();
                    if (null != handler) {
                        where = "";
                        Message message = new Message();
                        message.what = AMMETER_ERROR;
                        message.obj = "蓝牙连接断开";
                        handler.sendMessage(message);
                    }
                }
            } else {
                if (null != handler) {
                    where = "";
                    Message message = new Message();
                    message.what = AMMETER_ERROR;
                    message.obj = "蓝牙连接断开";
                    handler.sendMessage(message);
                }
            }
        }
    }

    /**
     * 返回按键监听
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * 返回按键监听
     */
    public Handler getHandler() {
        return handler;
    }

    protected void setReadAmmeterHandler(Handler readAmmeterHandler) {
        this.readAmmeterHandler = readAmmeterHandler;
    }

    protected void setReadConcentratorHandler(Handler readAmmeterHandler) {
        this.readConcentratorHandler = readAmmeterHandler;
    }

    public void stopConnectedThread() {
        if (null != mConnectedThread) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public static void destroyBTSocket() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /**
     * 该线程用于与远程设备的连接 处理与远程设备的输入输出
     */
    private class ConnectedThread extends Thread {
        private boolean flag = true;

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len;

            temp = null;
            temp = new byte[1024];
            tempLen = 0;
            byte abyte0[];


            // 保持监听输入流
            while (flag) {
                try {
                    // 从输入流中读取
                    len = mmInStream.read(buffer);
                    if (null != task) {
                        task.cancel();
                        timer.cancel();
                        task = null;
                        timer = null;
                    }
                    timer = new Timer();
                    task = new TimerTask() {

                        @Override
                        public void run() {
                            tempLen = 0;
                            temp = new byte[1024];
//								ReadAmmeterPluginForJava.stopElectricQuantity = true;
                            if (null != readAmmeterHandler) {
                                readAmmeterHandler.obtainMessage(-1, 0, 0,
                                        "").sendToTarget();
                            }
                            Log.w(TAG, "数据拼装失败，清除数据！！！ ");
                            // System.out.println(TAG + "数据拼装失败，清除数据！！！");
                        }
                    };
                    timer.schedule(task, 4000);
                    // Log.i(TAG, "数据返回，拼装数据");
                    for (int i = 0; i < len; i++) {

                        if ((buffer[i] & 0xFF) != 0x61) { // 从设备返回的流中，整理出完整数据格式的数组
                            temp[tempLen] = buffer[i];
                            tempLen++;
                        } else {
                            temp[tempLen] = buffer[i];
                            tempLen++;

                            if (temp[tempLen - 3] == FrameDataTool
                                    .convert(0x16)
                                    && temp[tempLen - 1] == FrameDataTool
                                    .convert(0x61)) {
                                //新加部分，设置终端
                                if(temp[4] == FrameDataTool.convert(0x68)&&temp[9] == FrameDataTool.convert(0x68)){
                                    Handler handler= MyHandle.getHandler();
                                    if(handler!=null) {
                                        Message message = Message.obtain();
                                        message.obj = temp;
                                        message.what = MyHandle.HANDLERCASE;
                                        handler.sendMessage(message);
                                    }
                                }
                                //到这结束
                                else {
                                    try {
                                        task.cancel();
                                        timer.cancel();
                                        task = null;
                                        timer = null;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    // 将读到的数据交给handler统一处理
                                    if (where.equals(WHERE_FROM_UPDATETERMINAL)) {
                                        terminal.convertMsg(temp);
                                    } else {
                                        handlerDeviceReturnData(temp, tempLen);
                                    }
                                    Log.i(TAG, "0x16接受到终端返回数据");
                                    tempLen = 0;
                                    temp = new byte[1024];
                                }
                            } else if (temp[1] != FrameDataTool
                                    .convert(0x83)) {
                                try {
                                    task.cancel();
                                    timer.cancel();
                                    task = null;
                                    timer = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // 将读到的数据交给handler统一处理
                                Log.i(TAG, "0x83接受到终端返回数据");
                                if (where.equals(WHERE_FROM_UPDATETERMINAL)) {
                                    terminal.convertMsg(temp);
                                    Log.i(TAG, "进入if判断");
                                } else {
                                    handlerDeviceReturnData(temp, tempLen);
                                    Log.i(TAG, "进入else判断");
                                }
                                tempLen = 0;
                                temp = new byte[1024];
                            }
                        }

                    }

                } catch (Exception e) {
                    // 通知页面改变连接状态
                    where = "";
                    flag = false;
                    BluetoothService.setState(BluetoothService.STATE_NONE);
                    socket = null;

                    if (null != handler) {
                        Message message = new Message();
                        message.what = AMMETER_ERROR;
                        message.obj = "蓝牙连接断开";
                        handler.sendMessage(message);
                    }
                    // connectionLost();
                    break;
                }
            }
        }

        /**
         * 连接时写入输出流
         *
         * @param buffer 需要写入的字节数组
         */
        public boolean write(byte[] buffer) {
            try {
                Log.i(TAG, "发送命令");
                mmOutStream.write(buffer);
                // 发送消息到UI Activity
                handler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
                return true;
            } catch (IOException e) {
                where = "";
                e.printStackTrace();
                flag = false;
                BluetoothService.setState(BluetoothService.STATE_NONE);
                socket = null;
                Message message = new Message();
                message.what = AMMETER_ERROR;
                message.obj = "蓝牙连接断开";
                handler.sendMessage(message);
                return false;
            }
        }

        public void cancel() {
            try {
                if (null != socket) {
                    socket.close();
                }
                if (mmInStream != null) {
                    mmInStream.close();
                    mmInStream = null;
                }
                if (mmOutStream != null) {
                    mmOutStream.close();
                    mmOutStream = null;
                }
            } catch (IOException e) {
                where = "";
                Log.e(TAG, "关闭输入流失败", e);
            }
        }

        public void setFlag() {
            flag = false;
        }
    }

    /**
     * 写不同步
     *
     * @param out 需要写入的字节数组
     * @see ConnectedThread#write(byte[])
     */
    public boolean write(byte[] out) {
        if (null == socket) {
            return false;
        }
        // 创建临时对象
        ConnectedThread r;
        // 与ConnectedThread同步
        if (mConnectedThread == null) {
            mConnectedThread = new ConnectedThread();
            mConnectedThread.start();
        }
        synchronized (this) {
            // 启动线程监听BluetoothServerSocket
            if (mConnectedThread == null) {
                // mConnectedThread = new ConnectedThread(socket);
                // mConnectedThread.start();
            }
            r = mConnectedThread;
        }
        // 写不同步
        return r.write(out);
    }

    private void handlerDeviceReturnData(byte[] data, int len) {
        // System.out.println("handlerDeviceReturnData ---> data =" + data
        // + "  data.length=" + data.length);

        if (data == null || data.length < 7) { // 如果数据为空或者数据区域长度没有达到帧类型的那一位

            return;
        }
        /*** 解析数据 **/
        byte dataHead = data[0]; // 取出数据的第一个数字
        Log.i(TAG,
                "解析数据--->dataHead=" + dataHead
                        + "  FrameDataTool.convert(0x86)="
                        + FrameDataTool.convert(0x86));
        // System.out.println("handlerDeviceReturnData ---> dataHead=" +
        // dataHead
        // + "  FrameDataTool.convert(0x86)="
        // + FrameDataTool.convert(0x86));

        if (dataHead != FrameDataTool.convert(0x86)) {
            return;
        }
        int datatype = data[1];
        int dataLen = data[2] < 0 ? 256 - data[2] : data[2]; // 数据域长度
        byte[] realData = new byte[dataLen]; // 定义真实的数据区域
        for (int i = 4; i < dataLen + 4; i++) { // 将数据区域取出来
            realData[i - 4] = data[i];
        }

        Log.i(TAG,
                "扫描条码数据--->datatype=" + datatype
                        + "  FrameDataTool.convert(0x81)="
                        + FrameDataTool.convert(0x81)
                        + "  FrameDataTool.convert(0x83)="
                        + FrameDataTool.convert(0x83)
                        + "  FrameDataTool.convert(0x88)="
                        + FrameDataTool.convert(0x88)
                        + "  FrameDataTool.convert(0x89)="
                        + FrameDataTool.convert(0x89));

        if (datatype == FrameDataTool.convert(0x81)) { // 扫描条码数据
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < dataLen; i++) { // 将数据区域取出来
                sb.append(realData[i] - 48); // 统一减去48后是条码的值
            }
            try {
                Log.i(TAG, "扫描条码数据 --->");

                if (null != handler) {
                    Log.i(TAG, "通知页面 ---> handler sb =" + sb.toString());

                    Message msg = new Message();
                    msg.what = AMMETER_SCANNING;
                    msg.obj = sb.toString();
                    handler.sendMessage(msg);
                    handler= ConfirmDialog.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = sb.toString();
                        message.what = ConfirmDialog.HANDLERCASEONE;
                        handler.sendMessage(message);
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }
        /** 解析表示数 **/
        else if (datatype == FrameDataTool.convert(0x83)) {
            try {
                Log.i(TAG,
                        "解析表示数 ---> realData.length =" + realData.length
                                + " data="
                                + dataConvertString(realData, realData.length));

                readAmmeterHandler.obtainMessage(2, 1, -1, realData)
                        .sendToTarget();

            } catch (Exception e) {
                Log.e(TAG, "解析表示数 error---> realData");
            }
        } else if (datatype == -61) {
//            Log.i(TAG, "解析表示数 ---> ReadAmmeterPluginForJava.testFlag ="
//                    + ReadAmmeterPluginForJava.testFlag);
//
//            if (ReadAmmeterPluginForJava.testFlag == -1) {
//                readAmmeterHandler.obtainMessage(2, 1, 97, "97").sendToTarget();
//            } else {
//                if (ReadAmmeterPluginForJava.testType.equals("97")) {
//                    readAmmeterHandler.obtainMessage(2, 1, 97, "97")
//                            .sendToTarget();
//                } else {
//                    readAmmeterHandler.obtainMessage(2, 1, 7, "07")
//                            .sendToTarget();
//                }
//            }
        } else if (datatype == FrameDataTool.convert(0x89)) { // 获取到按钮
            int keyValue = data[4];
            if (keyValue == FrameDataTool.convert(0x70)) { // 红外按钮

            } else if (keyValue == FrameDataTool.convert(0x0D)) { // OK按钮

            } else if (keyValue == FrameDataTool.convert(0x26)) { // 上按钮

            } else if (keyValue == FrameDataTool.convert(0x28)) { // 下按钮

            } else if (keyValue == FrameDataTool.convert(0x25)) { // 左按钮

            } else if (keyValue == FrameDataTool.convert(0x27)) { // 右按钮

            }
        } else if (datatype == FrameDataTool.convert(0x88)) { // 获得终端电量信息
            String powerStr = realData[0] + "%";
        } else {
            Log.i(TAG, "解析表示数 final else");
        }
    }

    /**
     * 将数据转换成string格式
     *
     * @param data
     * @return
     */
    private String dataConvertString(byte[] data, int len) {
        StringBuilder sb = new StringBuilder();
        String hex = null;
        for (int i = 0; i < len; i++) {
            hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            sb.append(hex.toUpperCase() + " ");
        }
        return sb.toString();
    }
}
