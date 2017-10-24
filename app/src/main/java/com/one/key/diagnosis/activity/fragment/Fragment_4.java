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

/**
 * 主站通信月流量
 * Created by 1611281 on 2017/10/13.
 */
/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 主站通信月流量
 */
public class Fragment_4 extends Fragment {
    private View view;
    public static Handler mHandler;
    public static Handler getHandler() {
        return mHandler;
    }
    //终端与主站当日通信流量
    byte[] ZDYZZDRLL= new byte[4];
    //终端与主站当月通信流量
    byte[] ZDYZZDYLL = new byte[4];
    private TextView mTxreadsimcardflow;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_mainstationmonthflow, null);
        intview();
        return view;
    }

    private void intview(){
        mTxreadsimcardflow = (TextView) view.findViewById(R.id.tx_read_simcard_flow);
        view.findViewById(R.id.Lssued).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASEFIVE;
                String simflowcmd = SendByteUtil.getInstance().splitMessage("7B","00000000FE","0C","00000201","");
                SendByteUtil.getInstance().sendByte(simflowcmd);
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                switch (msg.what) {
                    case MyHandle.HANDLERCASEFIVE:
                        System.arraycopy(temp, 22, ZDYZZDRLL, 0, 4);
                        System.arraycopy(temp, 26, ZDYZZDYLL, 0, 4);
                        String str = SendByteUtil.getInstance().tenTosix(ZDYZZDYLL[0])+SendByteUtil.getInstance().tenTosix(ZDYZZDYLL[1])
                                +SendByteUtil.getInstance().tenTosix(ZDYZZDYLL[2])+SendByteUtil.getInstance().tenTosix(ZDYZZDYLL[3]);
                        int txint = Integer.parseInt(str);
                        String txstr = String.valueOf(txint);
                        mTxreadsimcardflow.setText(txstr);
                        break;
                }
                return false;
            }
        });
    }
}
