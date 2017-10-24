package com.one.key.diagnosis.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by 1611281 on 2017/10/10.
 */

public class MyFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> listfragment;

    public MyFragmentAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.listfragment=list;
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
