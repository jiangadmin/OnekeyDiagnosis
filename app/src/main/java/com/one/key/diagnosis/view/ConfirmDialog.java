package com.one.key.diagnosis.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ikantech.support.util.YiDeviceUtils;
import com.one.key.diagnosis.R;

public class ConfirmDialog extends Dialog {

	private Context mContext;
	private Dialog dialog;
	private TextView txtitle;
	private TextView btnleft,btnright;
    private EditText ed_jzqaddress;
	private String text = "请输入集中器地址或扫描条码";
	private OnClickListener leftOnClickListener;
	private OnClickListener rightOnClickListener;
    public final static int HANDLERCASEONE = 1001;

    public static Handler mHandler;
    public static Handler getHandler() {
        return mHandler;
    }

	public ConfirmDialog(Context context) {
		super(context, R.style.Map_Info_Dialog_Style);
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialog = this;
		txtitle = (TextView)findViewById(R.id.title);
		txtitle.setText(text);
        ed_jzqaddress = (EditText)findViewById(R.id.ed_jzqaddress);
		btnleft = (TextView)findViewById(R.id.dialog_leftbtn);
		btnright = (TextView)findViewById(R.id.dialog_rightbtn);
		btnleft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				leftOnClickListener.onClick(dialog, 0);
			}
		});
		btnright.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rightOnClickListener.onClick(dialog, 1);
			}
		});
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLERCASEONE:
                        ed_jzqaddress.setText((String)msg.obj);
                        break;
                }
                return false;
            }
        });
	}
	
	public void setLeftBtn(OnClickListener listener){
		this.leftOnClickListener = listener;
	}
	
	public void setRightBtn(final OnClickListener listener){
		this.rightOnClickListener = listener;
	}
	
	public void setText(String text) {
		if (!TextUtils.isEmpty(text)) {
			this.text=text;
		}
	}

	public String getEditText(){
		return ed_jzqaddress.getText().toString();
	}

	public void close() {
		dialog.dismiss();
	}
	
	@Override
	 public void show() {
		 super.show();
//		 showDialog();
	 }
	public void showDialog(){
		DisplayMetrics dm = YiDeviceUtils
				.getDisplayMetrics((Activity) mContext);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		params.width = dm.widthPixels * 7 / 8;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		window.setAttributes(params);
	}

}

