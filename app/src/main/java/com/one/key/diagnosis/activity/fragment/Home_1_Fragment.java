package com.one.key.diagnosis.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.adapter.OnekeyDiagnosisAdapter;
import com.one.key.diagnosis.entity.MeasuringPoint;
import com.one.key.diagnosis.entity.Onekeydiagnosis;
import com.one.key.diagnosis.utils.LogUtil;
import com.one.key.diagnosis.utils.MyHandle;
import com.one.key.diagnosis.utils.SendByteUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.one.key.diagnosis.utils.MyHandle.HANDLERCASENINE;

/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 一键诊断
 */
public class Home_1_Fragment extends Fragment {
    private View view;
    protected Context context;
    private ListView listView;
    private OnekeyDiagnosisAdapter onekeyDiagnosisAdapter;
    private List<Onekeydiagnosis> onekeydiagnosisList= new ArrayList<Onekeydiagnosis>();;
    public static Handler mHandler;
    public static Handler getHandler() {
        return mHandler;
    }

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

    private List<MeasuringPoint> listmp = new ArrayList<MeasuringPoint>();
    private Onekeydiagnosis onekeydiagnosis;
    private static final Map<String,String> CLDmaps = new HashMap<String,String>() {{
        put("0", "无需对本序号的电能表或交流采样装置进行抄表");
        put("1", "DL/T 645—1997");put("2", "交流采样装置通信协议");
        put("30", "DL/T 645—2007");put("31", "串行接口连接窄带低压载波通信模块");}};

    //正向有功总电能示值
    byte[] ZXYGZDNSZ = new byte[5];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_onekeydiagnosis, null);
        context = getActivity();
        intview();
        return view;
    }

    private void intview(){
        listView = (ListView)view.findViewById(R.id.mylistview);
        onekeyDiagnosisAdapter = new OnekeyDiagnosisAdapter(context);
        listView.setAdapter(onekeyDiagnosisAdapter);
        view.findViewById(R.id.btn_star).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyHandle.HANDLERCASE = MyHandle.HANDLERCASESEVEN;
                String dadt ="00000201"+SendByteUtil.getInstance().toCld(0,12);
                dadt = dadt.replaceAll(" ", "");
                String measuringparcmd = SendByteUtil.getInstance().splitMessage("5B","01320100FE","0A",dadt,"");
                SendByteUtil.getInstance().sendByte(measuringparcmd);
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] temp = (byte[]) msg.obj;
                String str =  SendByteUtil.getInstance().bytesToHexString(temp, " ");

                Log.e("eeeeeeeeeeeeeeeeee","eeeee:----------------"+str);
                switch (msg.what) {
                    case MyHandle.HANDLERCASESEVEN:
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
                            onekeydiagnosis = new Onekeydiagnosis();
                            onekeydiagnosis.setMeasuringPointNum(SendByteUtil.getInstance().bytestoTen(CLDH));
                            String cldsljdkh = SendByteUtil.getInstance().tenToBcdStr2(CLDSLJDKH[0]).split(",")[1];
                            onekeydiagnosis.setPortNum(cldsljdkh);
                            onekeydiagnosis.setProtocolNum(CLDmaps.get(String.valueOf(CLDXYTYPE[0])));
                            onekeydiagnosis.setMeterAddress(SendByteUtil.getInstance().byteToStringAddress(CLDTXDZ));
                            onekeydiagnosisList.add(onekeydiagnosis);
                        }

                        onekeyDiagnosisAdapter.setDataList(onekeydiagnosisList);
                        if(onekeydiagnosisList.size()>0){
                            for (int i = 0;i<onekeydiagnosisList.size();i++){
                                if("31".equals(onekeydiagnosisList.get(i).getPortNum())){
                                    MyHandle.HANDLERCASE = HANDLERCASENINE;
                                    Date d = new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
                                    String dataUnit = sdf.format(d).replace("-","");
                                    String cmdrdj = SendByteUtil.getInstance().splitMessage("5B","01320100FE","0D","04010114",dataUnit);
                                    SendByteUtil.getInstance().sendByte(cmdrdj);
                                }else{
                                    MyHandle.HANDLERCASE = MyHandle.HANDLERCASEELEVEN;
                                    Date dzx = new Date();
                                    SimpleDateFormat sdfzx = new SimpleDateFormat("dd-MM-yy");
                                    String dataUnitzx = sdfzx.format(dzx).replace("-","");
                                    String cmdrdjzx = SendByteUtil.getInstance().splitMessage("5B","01320100FE","0D","04010100",dataUnitzx);
                                    SendByteUtil.getInstance().sendByte(cmdrdjzx);
                                }
                            }
                        }
                        break;
                    case MyHandle.HANDLERCASENINE:
                        System.arraycopy(temp, 31, ZXYGZDNSZ, 0, 5);
                        String zxygzdn = SendByteUtil.getInstance().byteToStringDY(ZXYGZDNSZ,10000);
                        break;
                }
                return false;
            }
        });
    }
}
