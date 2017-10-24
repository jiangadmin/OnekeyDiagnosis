package com.one.key.diagnosis.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by 1611281 on 2017/10/16.
 */

public class MyItemFragmentAdapter extends FragmentPagerAdapter {
    private FragmentManager fragmetnmanager;  //创建FragmentManager
    private List<Fragment> listfragment; //创建一个List<Fragment>
    private List<String> titleList;

    public MyItemFragmentAdapter(FragmentManager fm,List<Fragment> list, List<String> titleList) {
        super(fm);
        this.fragmetnmanager=fm;
        this.listfragment=list;
        this.titleList = titleList;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return listfragment.get(position);
    }

    @Override
    public int getCount() {
        return listfragment.size();
    }
}
