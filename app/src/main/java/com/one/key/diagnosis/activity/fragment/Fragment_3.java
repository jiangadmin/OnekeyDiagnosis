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
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 终端日历时钟
 */
public class Fragment_3 extends Fragment {
    private View view;
    public static Handler mHandler;
    public static Handler getHandler() {
        return mHandler;
    }
    //终端日历时钟
    byte[] ZDDATE = new byte[6];
    private TextView mtxtime;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_terminal_time, null);
        intview();
        return view;
    }

    private void intview(){
        mtxtime = (TextView) view.findViewById(R.id.tx_time);
        view.findViewById(R.id.Lssued).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASEFORE;
                String timecmd = SendByteUtil.getInstance().splitMessage("7A","00000000FE","0C","00000200","");
                SendByteUtil.getInstance().sendByte(timecmd);
            }
        });
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                switch (msg.what) {
                    case MyHandle.HANDLERCASEFORE:
                        System.arraycopy(temp, 22, ZDDATE, 0, 6);
                        mtxtime.setText(SendByteUtil.getInstance().byteToStringDate(ZDDATE));
                        break;
                }
                return false;
            }
        });
    }
}
