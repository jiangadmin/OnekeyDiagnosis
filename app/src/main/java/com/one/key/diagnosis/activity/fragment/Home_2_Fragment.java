package com.one.key.diagnosis.activity.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.one.key.diagnosis.BT.BTSocket;
import com.one.key.diagnosis.BT.BluetoothService;
import com.one.key.diagnosis.activity.MainActivity;
import com.one.key.diagnosis.R;
import com.one.key.diagnosis.activity.DeviceListActivity;
import com.one.key.diagnosis.utils.ConfigTool;

/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 设置
 */
public class Home_2_Fragment extends Fragment {
    private View view;
    public static Handler mHandler;
    public final static int HANDLERONE = 1001;
    public final static int HANDLERTWO = 1002;
    public final static int HANDLERTHREE = 1003;
    public static Handler getHandler() {
        return mHandler;
    }
    private TextView mtvdevicename;
    private Object synObj = new Object();
    private static final int FAILURE = 0 ;
    private static final int SUCCESS = 1 ;
    private static final int CONNECT_FAILURE = 2 ;
    private static final int CANCEL = 3 ; // 用户点击取消按钮
    private BluetoothAdapter bluetoothAdapter ;
    private String data ;// 扫描到的数据

    // services成员对象
    public static BluetoothService mService = null;
    // 已连接设备的名字
    public static String mConnectedDeviceName = null;
    // 设备MAC地址
    private String address = null;
    private Context context;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_setting, null);
        intview();
        return view;
    }

    private void intview(){
        mtvdevicename = (TextView)view.findViewById(R.id.tv_devicename);
        view.findViewById(R.id.tv_devicename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DeviceListActivity.class);
                intent.putExtra("type", "scan") ;
                startActivity(intent);
            }
        });
        view.findViewById(R.id.btn_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentdevice();
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLERONE:
                        mtvdevicename.setText((String)msg.obj);
                        break;
                    case MainActivity.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                Toast.makeText(context,"与连接" + mConnectedDeviceName + "成功",Toast.LENGTH_SHORT).show();
                                mService.setAddress(address);
                                weakup() ;
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                Toast.makeText(context,"与" + mConnectedDeviceName + "正在连接",Toast.LENGTH_SHORT).show();
                                break;
                            case BluetoothService.STATE_LISTEN:
                            case BluetoothService.STATE_NONE:
                                weakup() ;
                                break;
                            case BluetoothService.STATE_FAIL:
                                Toast.makeText(context,"与连接" + mConnectedDeviceName + "失败",Toast.LENGTH_SHORT).show();
                                break;
                            case BluetoothService.STATE_CONNECTEDED:
                                Toast.makeText(context,"设备已连接",Toast.LENGTH_SHORT).show();
                                break;
                        }
                        break;
                    case HANDLERTWO:
                        Toast.makeText(context, "请选择蓝牙设备", Toast.LENGTH_SHORT).show();
                        break;
                    case HANDLERTHREE:
                        Toast.makeText(context, "蓝牙连接已取消", Toast.LENGTH_SHORT).show();
                        break;
                    case BTSocket.AMMETER_ERROR:
                        Toast.makeText(context, "蓝牙连接已断开", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    public void contentdevice(){
        context = getContext().getApplicationContext() ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    address = null;

                    // 判断蓝牙是否打开
                    if (!bluetoothAdapter.isEnabled()) {
                        // 打开蓝牙连接
                        bluetoothAdapter.enable();

                    } else {
                        // 已连接，不需要再连接
                        if (BluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                            mService = BluetoothService.getInstance2(context, mHandler);
                            mService.setReturnDataToActivity(false);
                            Message message = Message.obtain();
                            message.what = MainActivity.MESSAGE_STATE_CHANGE;
                            message.arg1 = BluetoothService.STATE_CONNECTEDED;
                            mHandler.sendMessage(message);
                            return;
                        }
                    }

                    if(TextUtils.isEmpty(mtvdevicename.getText().toString())){
                        mHandler.obtainMessage(HANDLERTWO).sendToTarget();
                        return;
                    }
                    address = ConfigTool.getBlMac(context);

                    if (address == null || address.length() < 1) {
                        mHandler.obtainMessage(HANDLERTHREE).sendToTarget();
                        return;
                    }

                    if (mService == null || BluetoothService.getState() != BluetoothService.STATE_CONNECTED) {

                        // 初始化BluetoothService，进行蓝牙连接
                        BluetoothService.service = new BluetoothService(context);
                        mService = BluetoothService.getInstance2(context, mHandler);

                        // 获取BluetoothDevice对象
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                        // 获得连接的设备名称
                        mConnectedDeviceName = device.getName();
                        // 连接设备（条码扫描）
                        mService.pair(device);
                    }
                    sleep();

                    if(mService.getState() == BluetoothService.STATE_CONNECTED){
                    } else {
                        Message message = Message.obtain();
                        message.what = MainActivity.MESSAGE_STATE_CHANGE;
                        message.arg1 = BluetoothService.STATE_FAIL;
                        mHandler.sendMessage(message);
                        Thread.sleep(1000) ;
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sleep()
    {
        try
        {
            synchronized(synObj)
            {
                synObj.wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void weakup()
    {
        synchronized(synObj)
        {
            synObj.notify();
        }
    }
}