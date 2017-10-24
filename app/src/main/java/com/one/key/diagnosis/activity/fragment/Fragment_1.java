package com.one.key.diagnosis.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.utils.MyHandle;
import com.one.key.diagnosis.utils.SendByteUtil;
import com.one.key.diagnosis.utils.TabToast;

/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 终端IP
 */

public class Fragment_1 extends Fragment implements View.OnClickListener {
    private static final String TAG = "Fragment_1";

    public static Handler mHandler;

    public static Handler getHandler() {
        return mHandler;
    }

    //IP 端口 网关 子网掩码
    TextView ip, port, gateway, sm;

    //召测
    Button test;
    //终端IP
    byte[] ZDIP = new byte[4];
    //终端子网掩码
    byte[] ZDZWYM = new byte[4];
    //终端网关地址
    byte[] ZDWG = new byte[4];
    //终端监听端口(依照规约，最后两个字节为监听端口)
    byte[] ZDPort = new byte[2];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmeng_1, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initview();
    }

    private void initview() {
        ip = getActivity().findViewById(R.id.fragment_1_ip);
        port = getActivity().findViewById(R.id.fragment_1_port);
        gateway = getActivity().findViewById(R.id.fragment_1_gateway);
        sm = getActivity().findViewById(R.id.fragment_1_sm);

        test = getActivity().findViewById(R.id.fragment_1_test);

        test.setOnClickListener(this);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                switch (msg.what) {
                    case MyHandle.HANDLERCASEONE:
                        System.arraycopy(temp, 22, ZDIP, 0, 4);
                        System.arraycopy(temp, 26, ZDZWYM, 0, 4);
                        System.arraycopy(temp, 30, ZDWG, 0, 4);
                        System.arraycopy(temp, 44, ZDPort, 0, 2);
                        ip.setText(SendByteUtil.getInstance().bytesToIp(ZDIP));
                        port.setText(SendByteUtil.getInstance().bytestoTen(ZDPort));
                        gateway.setText(SendByteUtil.getInstance().bytesToIp(ZDWG));
                        sm.setText(SendByteUtil.getInstance().bytesToIp(ZDZWYM));
                        break;
                    default:
                        TabToast.makeText("错误");
                        break;
                }
                return false;
            }
        });
    }

    public void update() {
        ip.setText(SendByteUtil.getInstance().bytesToIp(ZDIP));
        port.setText(SendByteUtil.getInstance().bytestoTen(ZDPort));
        gateway.setText(SendByteUtil.getInstance().bytesToIp(ZDWG));
        sm.setText(SendByteUtil.getInstance().bytesToIp(ZDZWYM));
    }

    @Override
    public void onClick(View view) {
        MyHandle.HANDLERCASE = MyHandle.HANDLERCASEONE;
        String ipcmd = SendByteUtil.getInstance().splitMessage("7B", "4513000300", "0A", "00004000", "");
        SendByteUtil.getInstance().sendByte(ipcmd);
    }
}