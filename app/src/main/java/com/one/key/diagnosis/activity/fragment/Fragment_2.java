package com.one.key.diagnosis.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
 * Purpose: TODO 主站IP
 */
public class Fragment_2 extends Fragment implements View.OnClickListener {
    private static final String TAG = "Fragment_2";
    public static Handler mHandler;

    public static Handler getHandler() {
        return mHandler;
    }

    //确认帧
    byte[] CONFIRM = new byte[1];
    //主站IP
    byte[] ZZIP = new byte[4];
    //主站端口
    byte[] ZZPort = new byte[2];
    //主站备用IP
    byte[] ZZBYIP = new byte[4];
    //主站备用端口
    byte[] ZZBYPort = new byte[2];
    //主站APN
    byte[] ZZAPN = new byte[16];

    private TextView mTextzzip, mTextzzport, mTextzzbyip, mTextzzbyport, mTextzzapn;
    private EditText text_zzip, text_zzport, text_zzbyip, text_zzbyport, text_zzapn;

    //召测 下发
    Button Measure, Lssued;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_mainstation_ip, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initview();
    }

    public void initview() {
        mTextzzip = getActivity().findViewById(R.id.textZZIP);
        mTextzzport = getActivity().findViewById(R.id.textzzDK);
        mTextzzbyip = getActivity().findViewById(R.id.textbyzzIP);
        mTextzzbyport = getActivity().findViewById(R.id.textbyzzPort);
        mTextzzapn = getActivity().findViewById(R.id.textAPN);

        text_zzip = getActivity().findViewById(R.id.text_zzip);
        text_zzport = getActivity().findViewById(R.id.text_zzport);
        text_zzbyip = getActivity().findViewById(R.id.text_zzbyip);
        text_zzbyport = getActivity().findViewById(R.id.text_zzbyport);
        text_zzapn = getActivity().findViewById(R.id.text_zzapn);

        Measure = getActivity().findViewById(R.id.Measure);
        Lssued = getActivity().findViewById(R.id.Lssued);

        Measure.setOnClickListener(this);
        Lssued.setOnClickListener(this);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                switch (msg.what) {
                    case MyHandle.HANDLERCASETWO:
                        System.arraycopy(temp, 22, ZZIP, 0, 4);
                        System.arraycopy(temp, 26, ZZPort, 0, 2);
                        System.arraycopy(temp, 28, ZZBYIP, 0, 4);
                        System.arraycopy(temp, 32, ZZBYPort, 0, 2);
                        System.arraycopy(temp, 34, ZZAPN, 0, 16);
                        mTextzzip.setText(SendByteUtil.getInstance().bytesToIp(ZZIP));
                        mTextzzport.setText(SendByteUtil.getInstance().bytestoTen(ZZPort));
                        mTextzzbyip.setText(SendByteUtil.getInstance().bytesToIp(ZZBYIP));
                        mTextzzbyport.setText(SendByteUtil.getInstance().bytestoTen(ZZBYPort));
                        mTextzzapn.setText(SendByteUtil.getInstance().bytetoAPn(ZZAPN));
                        break;
                    case MyHandle.HANDLERCASETHREE:
                        System.arraycopy(temp, 20, CONFIRM, 0, 1);
                        if (CONFIRM[0] == 1)
                            TabToast.makeText("设置成功！");
                        else
                            TabToast.makeText("设置失败！");

                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //召测
            case R.id.Measure:
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASETWO;
                String zzipcmd = SendByteUtil.getInstance().splitMessage("5B", "00000000FE", "0A", "00000400", "");
                SendByteUtil.getInstance().sendByte(zzipcmd);
                break;
            //下发
            case R.id.Lssued:
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASETHREE;
                String zzip = text_zzip.getText().toString().trim();
                String zzport = text_zzport.getText().toString().trim();
                String zzbyip = text_zzbyip.getText().toString().trim();
                String zzbyport = text_zzbyport.getText().toString().trim();
                String zzapn = text_zzapn.getText().toString().trim();
                String ZZPW = "00000000000000000000000000007714";
                if (TextUtils.isEmpty(zzip)){
                    TabToast.makeText(String.format("%1$s数据缺失","主站IP"));
                    return;
                }
                if (TextUtils.isEmpty(zzport)){
                    TabToast.makeText(String.format("%1$s数据缺失","主站端口"));
                    return;
                }
                if (TextUtils.isEmpty(zzbyip)){
                    TabToast.makeText(String.format("%1$s数据缺失","备用IP"));
                    return;
                }
                if (TextUtils.isEmpty(zzbyport)){
                    TabToast.makeText(String.format("%1$s数据缺失","备用端口"));
                    return;
                }
                if (TextUtils.isEmpty(zzapn)){
                    TabToast.makeText(String.format("%1$s数据缺失","APN"));
                    return;
                }

                String zzdataunit = SendByteUtil.getInstance().SplicedIp(zzip) + SendByteUtil.getInstance().getPort(zzport)
                        + SendByteUtil.getInstance().SplicedIp(zzbyip) + SendByteUtil.getInstance().getPort(zzbyport)
                        + SendByteUtil.getInstance().apntoSixteen(zzapn) + ZZPW;
                String setzzmasteripcmd = SendByteUtil.getInstance().splitMessage("7B", "00000000FE", "04", "00000400", zzdataunit);
                SendByteUtil.getInstance().sendByte(setzzmasteripcmd);
                break;

        }
    }
}
