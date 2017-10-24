package com.one.key.diagnosis.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.entity.Onekeydiagnosis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1611281 on 2017/10/17.
 */

public class OnekeyDiagnosisAdapter extends BaseAdapter {
    private Context mcontext;
    private LayoutInflater mInflater;
    private List<Onekeydiagnosis> onekeydiagnosisList = new ArrayList<Onekeydiagnosis>();

    public OnekeyDiagnosisAdapter(Context context){
        this.mcontext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setDataList(List<Onekeydiagnosis> onekeydiagnosisList){
        this.onekeydiagnosisList.clear();
        if(onekeydiagnosisList!=null && onekeydiagnosisList.size()>0){
            this.onekeydiagnosisList.addAll(onekeydiagnosisList);
        }
        notifyDataSetChanged();
    }

    public void clearDataList(){
        this.onekeydiagnosisList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return onekeydiagnosisList.size();
    }

    @Override
    public Onekeydiagnosis getItem(int i) {
        return onekeydiagnosisList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(view == null) {
            view = mInflater.inflate(R.layout.layout_onekey_item, null);
            holder = new ViewHolder();
            holder.tvmeasuringPointNum = view.findViewById(R.id.tv_measuringPointNum);
            holder.tvmeterAddress = view.findViewById(R.id.tv_meterAddress);
            holder.tvprotocolNum = view.findViewById(R.id.tv_protocolNum);
            holder.tvportNum = view.findViewById(R.id.tv_portNum);
            holder.tvdayfrozenValue = view.findViewById(R.id.tv_dayfrozenValue);
            holder.tvclockpassthrough = view.findViewById(R.id.tv_clockpassthrough);
            holder.tvdiagnosisResult = view.findViewById(R.id.tv_diagnosisResult);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        if(view != null) {
            Onekeydiagnosis onekeydiagnosis = getItem(i);
            holder.tvmeasuringPointNum.setText(onekeydiagnosis.getMeasuringPointNum());
            holder.tvmeterAddress.setText(onekeydiagnosis.getMeterAddress());
            holder.tvprotocolNum.setText(onekeydiagnosis.getProtocolNum());
            holder.tvportNum.setText(onekeydiagnosis.getPortNum());
            holder.tvdayfrozenValue.setText(onekeydiagnosis.getDayfrozenValue());
            holder.tvclockpassthrough.setText(onekeydiagnosis.getClockpassthrough());
            holder.tvdiagnosisResult.setText(onekeydiagnosis.getDiagnosisResult());
        }

        //隔行变色
        if (i % 2 == 0){
            view.setBackgroundResource(R.color.white);
        }else{
            view.setBackgroundResource(R.color.read_param_corlor);
        }

        return view;
    }

    class ViewHolder {
        TextView tvmeasuringPointNum;
        TextView tvmeterAddress;
        TextView tvprotocolNum;
        TextView tvportNum;
        TextView tvdayfrozenValue;
        TextView tvclockpassthrough;
        TextView tvdiagnosisResult;
    }
}
