package com.one.key.diagnosis.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.activity.fragment.Home_2_Fragment;
import com.one.key.diagnosis.utils.ConfigTool;

import java.util.Set;

/**
 * 
 * 该Activity是一个对话框，该对话框列出了所有配对过设备和在该地区检测到的设备，
 * 当用户选择一个设备后，该设备的MAC地址发送回父Activity
 * 
 */
public class DeviceListActivity extends Activity {
    private static final String TAG = "DeviceListActivity";

    // Intent extra 返回的值
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // 成员变量
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    
    public static String address ;
    
    private String type = ""; // 扫描连接

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.layout_device_list_activity);

        // 点击返回时调用
        setResult(Activity.RESULT_CANCELED);
        
        if(getIntent().hasExtra("type")){
        	type = getIntent().getStringExtra("type") ;
        }

        // 搜索设备
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setEnabled(false) ;
            }
        });
        
        // 取消按钮
        Button disconnect = (Button) findViewById(R.id.deviceListActivityCancel) ;
        disconnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		}) ;

        // 初始化ArrayAdapter，一个用于已经配对的设备，一个用于新发现的设备
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // 显示已配对过的设备
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // 显示查找到的设备
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // 当发现设备时注册广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // 当查找设备完成时注册广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // 得到本地Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter != null && !mBtAdapter.isEnabled()) {
        	mBtAdapter.enable();
        }

        // 获取已配对设备的集合
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 如果有配对过的设备，添加到ArrayAdapter中
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 确定没有查找其他设备
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // 注销广播监听器
        this.unregisterReceiver(mReceiver);
    }

    /**
     * 通过BluetoothAdapter开始查找设备
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        
        // 查找到设备后，停止查找
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // 通过BluetoothAdapter请求查找设备
        mBtAdapter.startDiscovery();
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 停止查找
            mBtAdapter.cancelDiscovery();

            // 得到设备的MAC地址
            String info = ((TextView) v).getText().toString();
            String none_paired = getResources().getString(R.string.none_paired) ;
            String none_found = getResources().getString(R.string.none_found) ;
            
            if(!none_paired.equals(info) && !none_found.equals(info)){
            	
            	address = info.substring(info.length() - 17);
                if(type.equals("scan")){
                    ConfigTool.setBlMac(DeviceListActivity.this.getApplicationContext(), address);
                    Handler handler = Home_2_Fragment.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = address;
                        message.what = Home_2_Fragment.HANDLERONE;
                        handler.sendMessage(message);
                    }
                }
            	finish();
            }
        }
    };

    // 该广播监听设备查找完成，并改变头部状态
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从Intent中获取BluetoothDevice对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果已经配对，则跳过，因为列表中已经存在
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // 当查找完成，改变BTPrinterActivity头部文本
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}

