package com.one.key.diagnosis.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.entity.MeasuringPoint;
import com.one.key.diagnosis.utils.MyHandle;
import com.one.key.diagnosis.utils.SendByteUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 1611281 on 2017/10/23.
 */
/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 终端其它信息
 */
public class Fragment_6 extends Fragment implements View.OnClickListener{
    private View view;
    private TextView textView;
    public static Handler mHandler;
    public static Handler getHandler() {
        return mHandler;
    }
    //确认帧
    byte[] CONFIRM = new byte[1];
    //数据时标
    byte[] SJSB = new byte[3];
    //终端抄表时间
    byte[] ZDCBSJ = new byte[5];
    //费率数
    byte[] FLS = new byte[1];
    //正向有功总电能示值
    byte[] ZXYGZDNSZ = new byte[5];
    //费率正向有功电能示值
    byte[] FLZXYGDNSZ = new byte[5];
    //一象限无功能示值
    byte[] YXXWGNSZ = new byte[4];
    //四象限无功能示值
    byte[] SXXWGNSZ = new byte[4];

    //本次电能表/交流采样装置配置数量
    byte[] CLDPXSL = new byte[2];
    //电能表/交流采样装置序号
    byte[] CLDXH = new byte[2];
    //所属测量点号
    byte[] CLDH = new byte[2];
    //通信速率及端口号
    byte[] CLDSLJDKH = new byte[1];
    //通信协议类型
    byte[] CLDXYTYPE = new byte[1];
    //通信地址
    byte[] CLDTXDZ = new byte[6];
    //通信密码
    byte[] CLDPASSWORD = new byte[6];
    //电能费率个数
    byte[] CLDFLGS = new byte[1];
    //有功电能示值整数位及小数位个数
    byte[] CLDYGDN = new byte[1];
    //所属采集器通信地址
    byte[] CLDTXADDRESS = new byte[6];
    //用户大类号及用户小类号
    byte[] CLDYHDXLH = new byte[1];

    String s ="";

    private List<MeasuringPoint> listmp = new ArrayList<MeasuringPoint>();
    private static final Map<String,String> CLDmaps = new HashMap<String,String>() {{
        put("0", "无需对本序号的电能表或交流采样装置进行抄表");
        put("1", "DL/T 645—1997");put("2", "交流采样装置通信协议");
        put("30", "DL/T 645—2007");put("31", "串行接口连接窄带低压载波通信模块");}};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_termainalother, null);
        intview();
        return view;
    }

    private void intview(){
        view.findViewById(R.id.confirm_reset).setOnClickListener(this);
        view.findViewById(R.id.confirm_immediatelyread).setOnClickListener(this);
        view.findViewById(R.id.btnjlcjxx).setOnClickListener(this);
        view.findViewById(R.id.btn_rdj).setOnClickListener(this);
        view.findViewById(R.id.btn_zxdnsz).setOnClickListener(this);
        textView = (TextView)view.findViewById(R.id.tv_test);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                String str11 =  SendByteUtil.getInstance().bytesToHexString(temp, " ");
                Log.e("eeeeeeeeeeeeeeeeee","eeeee:----------------"+str11);
                switch (msg.what) {
                    case MyHandle.HANDLERCASEEIGHT:
                        System.arraycopy(temp, 20, CONFIRM, 0, 1);
                        if (CONFIRM[0] == 1) {
                            Toast.makeText(getContext(), "设置成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "设置失败！", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MyHandle.HANDLERCASENINE:
                        System.arraycopy(temp, 22, SJSB, 0, 3);
                        System.arraycopy(temp, 25, ZDCBSJ, 0, 5);
                        System.arraycopy(temp, 30, FLS, 0, 1);
                        int count = Integer.parseInt(SendByteUtil.getInstance().tenTosix(FLS[0]));
                        System.arraycopy(temp, 31, ZXYGZDNSZ, 0, 5);
                        String str  = "数据时标："+SendByteUtil.getInstance().byteToStringDate2(SJSB)+"终端抄表时间："+SendByteUtil.getInstance().byteToStringDate3(ZDCBSJ)
                                +"费率数："+SendByteUtil.getInstance().tenTosix(FLS[0])+"正向有功总电能示值："+SendByteUtil.getInstance().byteToStringDY(ZXYGZDNSZ,10000);
                        for (int i=0;i<count;i++){
                            System.arraycopy(temp, 36+(i*5), FLZXYGDNSZ, 0, 5);
                            str+="费率"+(i+1)+"正向有功电能示值："+ SendByteUtil.getInstance().byteToStringDY(FLZXYGDNSZ,10000);
                        }
                        //System.arraycopy(temp, 20, FLZXYGDNSZ, 0, 5);
                        //String str =  SendByteUtil.getInstance().bytesToHexString(temp, " ");
                        Log.e("eeeeeeeeeeeeeeeeee","eeeee:----------------"+str);
                        break;
                    case MyHandle.HANDLERCASETEN:
                        System.arraycopy(temp, 22, CLDPXSL, 0, 2);
                        int count1 = Integer.parseInt(SendByteUtil.getInstance().bytestoTen(CLDPXSL));
                        MeasuringPoint measuringPoint;
                        listmp.clear();
                        for (int i = 0; i< count1; i++){
                            System.arraycopy(temp, 24+(i*27), CLDXH, 0, 2);
                            System.arraycopy(temp, 26+(i*27), CLDH, 0, 2);
                            System.arraycopy(temp, 28+(i*27), CLDSLJDKH, 0, 1);
                            System.arraycopy(temp, 29+(i*27), CLDXYTYPE, 0, 1);
                            System.arraycopy(temp, 30+(i*27), CLDTXDZ, 0, 6);
                            System.arraycopy(temp, 36+(i*27), CLDPASSWORD, 0, 6);
                            System.arraycopy(temp, 42+(i*27), CLDFLGS, 0, 1);
                            System.arraycopy(temp, 43+(i*27), CLDYGDN, 0, 1);
                            System.arraycopy(temp, 44+(i*27), CLDTXADDRESS, 0, 6);
                            System.arraycopy(temp, 50+(i*27), CLDYHDXLH, 0, 1);
                            measuringPoint = new MeasuringPoint();
                            measuringPoint.setCldxh(SendByteUtil.getInstance().bytestoTen(CLDXH));
                            measuringPoint.setCldh(SendByteUtil.getInstance().bytestoTen(CLDH));
                            measuringPoint.setCldsljdkh(SendByteUtil.getInstance().tenToBcdStr2(CLDSLJDKH[0]));
                            measuringPoint.setCldxytype(CLDmaps.get(String.valueOf(CLDXYTYPE[0])));
                            measuringPoint.setCldtxdz(SendByteUtil.getInstance().byteToStringAddress(CLDTXDZ));
                            measuringPoint.setCldpassword(SendByteUtil.getInstance().tocldtxpass(CLDPASSWORD));
                            measuringPoint.setCldtxaddress(SendByteUtil.getInstance().byteToStringAddress(CLDTXADDRESS));
                            listmp.add(measuringPoint);
                        }
                        for (int i = 0; i<listmp.size() ; i++){
                            s += "测量点序号:"+listmp.get(i).getCldxh()+",测量点号:"+listmp.get(i).getCldh()+",速率端口:"+listmp.get(i).getCldsljdkh()
                                    +",型号:"+listmp.get(i).getCldxytype()+",通讯地址:"+listmp.get(i).getCldtxdz()+",通信密码:"+listmp.get(i).getCldpassword()+",所属通讯地址:"+listmp.get(i).getCldtxaddress();
                        }
                        textView.setText(s);
                        Log.e("eeeeeeeeeeeeeeeeee","eeeee:----------------"+s);
                        break;
                    case MyHandle.HANDLERCASEELEVEN:
                        System.arraycopy(temp, 22, SJSB, 0, 3);
                        System.arraycopy(temp, 25, ZDCBSJ, 0, 5);
                        System.arraycopy(temp, 30, FLS, 0, 1);
                        int countzx = Integer.parseInt(SendByteUtil.getInstance().tenTosix(FLS[0]));
                        System.arraycopy(temp, 31, ZXYGZDNSZ, 0, 5);
                        String strzx  = "数据时标："+SendByteUtil.getInstance().byteToStringDate2(SJSB)+"终端抄表时间："+SendByteUtil.getInstance().byteToStringDate3(ZDCBSJ)
                                +"费率数："+SendByteUtil.getInstance().tenTosix(FLS[0])+"正向有功总电能示值："+SendByteUtil.getInstance().byteToStringDY(ZXYGZDNSZ,10000);
                        for (int i=0;i<countzx;i++){
                            System.arraycopy(temp, 36+(i*5), FLZXYGDNSZ, 0, 5);
                            strzx+="费率"+(i+1)+"正向有功电能示值："+ SendByteUtil.getInstance().byteToStringDY(FLZXYGDNSZ,10000);
                        }
                        for (int i=0;i<countzx;i++){
                            System.arraycopy(temp, 36+(countzx*5)+(i*4), YXXWGNSZ, 0, 4);
                            strzx+="费率"+(i+1)+"一象无功电能示值："+ SendByteUtil.getInstance().byteToStringDY(YXXWGNSZ,100);
                        }
                        for (int i=0;i<countzx;i++){
                            System.arraycopy(temp, 36+(countzx*5)+(countzx*4)+(i*4), YXXWGNSZ, 0, 4);
                            strzx+="费率"+(i+1)+"四象无功电能示值："+ SendByteUtil.getInstance().byteToStringDY(YXXWGNSZ,100);
                        }
                        Log.e("eeeeeeeeeeeeeeeeee","eeeee:----------------"+strzx);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm_reset://复位
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASEEIGHT;
                String cmdfw = SendByteUtil.getInstance().splitMessage("5B","00000000FE","01","00000100","");
                SendByteUtil.getInstance().sendByte(cmdfw);
                break;
            case R.id.confirm_immediatelyread://终端立即抄表
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASEEIGHT;
                String ZDPW = "00000000000000000000000000007714";
                String zddataunit = "01"+ZDPW;
                String cmdljcb = SendByteUtil.getInstance().splitMessage("7B","00000000FE","05","00000406",zddataunit);
                SendByteUtil.getInstance().sendByte(cmdljcb);
                break;
            case R.id.btnjlcjxx:
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASETEN;
                for (int i = 0;i<4;i++){
                    String dadt ="00000201"+SendByteUtil.getInstance().toCld(i,1);
                    dadt = dadt.replaceAll(" ", "");
                    String measuringparcmd = SendByteUtil.getInstance().splitMessage("5B","01320100FE","0A",dadt,"");
                    SendByteUtil.getInstance().sendByte(measuringparcmd);
                }
                break;
            case R.id.btn_rdj:
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASENINE;
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
                String dataUnit = sdf.format(d).replace("-","");
                String cmdrdj = SendByteUtil.getInstance().splitMessage("5B","01320100FE","0D","04010114",dataUnit);
                SendByteUtil.getInstance().sendByte(cmdrdj);
                break;
            case R.id.btn_zxdnsz:
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASEELEVEN;
                Date dzx = new Date();
                SimpleDateFormat sdfzx = new SimpleDateFormat("dd-MM-yy");
                String dataUnitzx = sdfzx.format(dzx).replace("-","");
                String cmdrdjzx = SendByteUtil.getInstance().splitMessage("5B","01320100FE","0D","04010100",dataUnitzx);
                SendByteUtil.getInstance().sendByte(cmdrdjzx);
                break;
        }
    }
}
