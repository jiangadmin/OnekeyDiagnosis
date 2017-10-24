package com.one.key.diagnosis.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.utils.MyHandle;
import com.one.key.diagnosis.utils.SendByteUtil;

import static com.one.key.diagnosis.utils.MyHandle.HANDLERCASESIX;

/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 终端版本信息
 */
public class Fragment_5 extends Fragment {
    private View view;
    public static Handler mHandler;
    public static Handler getHandler() {
        return mHandler;
    }
    //终端版本信息-厂商代号
    byte[] CSDH = new byte[4];
    //终端版本信息-设备编号
    byte[] SBBH = new byte[8];
    //终端版本信息-终端软件版本号
    byte[] ZDRJBBH = new byte[4];
    //终端版本信息-终端软件发布日期
    byte[] ZDRJFBDATE = new byte[3];
    //终端版本信息-终端配置容量信息码
    byte[] ZDPZRLXXNUM = new byte[11];
    //终端版本信息-终端通信协议版本号
    byte[] ZDTXXYBBNUM = new byte[4];
    //终端版本信息-终端硬件版本号
    byte[] ZDYJBBNUM = new byte[4];
    //终端版本信息-终端硬件发布日期
    byte[] ZDYJFBDATE = new byte[3];
    private TextView mtextcsdh,mtextsbbh,mtextzdrjbbh,mtextzdrjfbdate,mtextzdpzrlxxnum,mtextzdtxxybbnum,
            mtextzdyjbbnum,mtextzdyjfbdate;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_terminalversioninfor, null);
        intview();
        return view;
    }

    private void intview(){
        mtextcsdh = (TextView) view.findViewById(R.id.tx_csdh);
        mtextsbbh = (TextView) view.findViewById(R.id.tx_sbbh);
        mtextzdrjbbh = (TextView) view.findViewById(R.id.tx_zdrjbbh);
        mtextzdrjfbdate = (TextView) view.findViewById(R.id.tx_zdrjfbdate);
        mtextzdpzrlxxnum = (TextView) view.findViewById(R.id.tx_zdpzrlxxnum);
        mtextzdtxxybbnum = (TextView) view.findViewById(R.id.tx_zdtxxybbnum);
        mtextzdyjbbnum = (TextView) view.findViewById(R.id.tx_zdyjbbnum);
        mtextzdyjfbdate = (TextView) view.findViewById(R.id.tx_zdyjfbdate);
        view.findViewById(R.id.Lssued).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASESIX;
                String versioncmd = SendByteUtil.getInstance().splitMessage("7B","00000000FE","09","00000100","");
                SendByteUtil.getInstance().sendByte(versioncmd);

            }
        });
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                switch (msg.what) {
                    case HANDLERCASESIX:
                        System.arraycopy(temp, 22, CSDH, 0, 4);
                        System.arraycopy(temp, 26, SBBH, 0, 8);
                        System.arraycopy(temp, 34, ZDRJBBH, 0, 4);
                        System.arraycopy(temp, 38, ZDRJFBDATE, 0, 3);
                        System.arraycopy(temp, 41, ZDPZRLXXNUM, 0, 11);
                        System.arraycopy(temp, 52, ZDTXXYBBNUM, 0, 4);
                        System.arraycopy(temp, 56, ZDYJBBNUM, 0, 4);
                        System.arraycopy(temp, 60, ZDYJFBDATE, 0, 3);
                        mtextcsdh.setText(SendByteUtil.getInstance().bytetoAPn(CSDH));
                        mtextcsdh.setText(SendByteUtil.getInstance().bytetoAPn(SBBH));
                        mtextzdrjbbh.setText(SendByteUtil.getInstance().bytetoAPn(ZDRJBBH));
                        mtextzdrjfbdate.setText(SendByteUtil.getInstance().byteToStringDate2(ZDRJFBDATE));
                        mtextzdpzrlxxnum.setText(SendByteUtil.getInstance().bytetoAPn(ZDPZRLXXNUM));
                        mtextzdtxxybbnum.setText(SendByteUtil.getInstance().bytetoAPn(ZDTXXYBBNUM));
                        mtextzdyjbbnum.setText(SendByteUtil.getInstance().bytetoAPn(ZDYJBBNUM));
                        mtextzdyjfbdate.setText(SendByteUtil.getInstance().byteToStringDate2(ZDYJFBDATE));
                        break;
                }
                return false;
            }
        });
    }
}
