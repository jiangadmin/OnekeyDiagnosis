package com.one.key.diagnosis.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.adapter.MyItemFragmentAdapter;
import com.one.key.diagnosis.utils.ViewFindUtils;
import com.one.key.diagnosis.view.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 * on 17/10/23.
 * Email:
 * Phone：
 * Purpose: TODO 参数召测和设置
 */
public class Home_3_Fragment extends Fragment implements ViewPager.OnPageChangeListener{

    private Fragment_1 terminalIpFragment;
    private Fragment_2 mainstationIpFragment;
    private Fragment_3 terminalTimeFragment;
    private Fragment_4 mainstationmonthflowFragment;
    private Fragment_5 terminalversioninforFragment;
    private Fragment_6 terminalOtherFragment;

    private List<Fragment> listfragment = new ArrayList();
    private String[] titleStr = {"终端IP", "主站IP", "终端日历时钟","主站通信月流量","终端版本信息","终端其他信息"};
    private List<String> titleList = null;

    private View view;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_measureandset, null);
        intview();
        return view;
    }

    private void intview(){
        viewPager = ViewFindUtils.find(view, R.id.myitemviewpager);
        terminalIpFragment = new Fragment_1();
        mainstationIpFragment = new Fragment_2();
        terminalTimeFragment = new Fragment_3();
        mainstationmonthflowFragment = new Fragment_4();
        terminalversioninforFragment = new Fragment_5();
        terminalOtherFragment = new Fragment_6();
        listfragment.add(terminalIpFragment);
        listfragment.add(mainstationIpFragment);
        listfragment.add(terminalTimeFragment);
        listfragment.add(mainstationmonthflowFragment);
        listfragment.add(terminalversioninforFragment);
        listfragment.add(terminalOtherFragment);

        titleList = new ArrayList<>();
        for (int i = 0; i < titleStr.length; i++) titleList.add(titleStr[i]);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        MyItemFragmentAdapter mfpa=new MyItemFragmentAdapter(fm, listfragment,titleList);
        viewPager.setAdapter(mfpa);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(this);

        SlidingTabLayout tabLayout_4 = ViewFindUtils.find(view, R.id.slidingtabkayout);
        tabLayout_4.setViewPager(viewPager);


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
