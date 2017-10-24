package com.one.key.diagnosis.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.one.key.diagnosis.activity.fragment.Fragment_2;
import com.one.key.diagnosis.activity.fragment.Fragment_4;
import com.one.key.diagnosis.activity.fragment.Home_1_Fragment;
import com.one.key.diagnosis.activity.fragment.Fragment_1;
import com.one.key.diagnosis.activity.fragment.Fragment_6;
import com.one.key.diagnosis.activity.fragment.Fragment_3;
import com.one.key.diagnosis.activity.fragment.Fragment_5;

/**
 * Created by 1611281 on 2017/10/18.
 */

public class MyHandle {

    public static int HANDLERCASE = 0;
    public final static int HANDLERCASEZERO = 1000;
    public final static int HANDLERCASEONE = 1001;
    public final static int HANDLERCASETWO = 1002;
    public final static int HANDLERCASETHREE = 1003;
    public final static int HANDLERCASEFORE = 1004;
    public final static int HANDLERCASEFIVE = 1005;
    public final static int HANDLERCASESIX = 1006;
    public final static int HANDLERCASESEVEN = 1007;
    public final static int HANDLERCASEEIGHT = 1008;
    public final static int HANDLERCASENINE = 1009;
    public final static int HANDLERCASETEN = 1010;
    public final static int HANDLERCASEELEVEN = 1011;

    public static Handler getHandler() {
        return mHandler;
    }

    @SuppressLint("HandlerLeak")
    public static Handler mHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            byte[] temp = (byte[]) msg.obj;
            Handler handler = null;
            switch (msg.what) {
                case HANDLERCASEZERO:
                    break;
                case HANDLERCASEONE:
                    handler = Fragment_1.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASETWO:
                    handler= Fragment_2.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASETHREE:
                    handler= Fragment_2.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASEFORE:
                    handler= Fragment_3.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASEFIVE:
                    handler= Fragment_4.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASESIX:
                    handler= Fragment_5.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASESEVEN:
                    handler= Home_1_Fragment.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASEEIGHT:
                    handler= Fragment_6.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASENINE:
                    handler= Home_1_Fragment.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASETEN:
                    handler= Fragment_6.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
                case HANDLERCASEELEVEN:
                    handler= Home_1_Fragment.getHandler();
                    if(handler!=null) {
                        Message message = Message.obtain();
                        message.obj = temp;
                        message.what = MyHandle.HANDLERCASE;
                        handler.sendMessage(message);
                    }
                    break;
            }
        }
    };
}
