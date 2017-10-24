package com.one.key.diagnosis.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.one.key.diagnosis.R;
import com.one.key.diagnosis.activity.fragment.Home_1_Fragment;
import com.one.key.diagnosis.activity.fragment.Home_2_Fragment;
import com.one.key.diagnosis.activity.fragment.Home_3_Fragment;
import com.one.key.diagnosis.utils.TabToast;
import com.one.key.diagnosis.view.ConfirmDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentTransaction mFragmentTransaction;//fragment事务
    FragmentManager mFragmentManager;//fragment管理者

    Home_1_Fragment home_1_fragment;
    Home_2_Fragment home_2_fragment;
    Home_3_Fragment home_3_fragment;

    private ConfirmDialog mdialog = null;
    private LinearLayout linview;
    private EditText editText;

    // BluetoothService发送过来的消息
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // 从BluetoothService接收到得名字
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText = (EditText) findViewById(R.id.ed_jzqaddress);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        intview();
    }

    private void intview() {
        mFragmentManager = getSupportFragmentManager();//获取到fragment的管理对象
        ShowFragment(1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void CreateDialog() {
        mdialog = new ConfirmDialog(this);
        linview = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog_addressread, null);
        ViewGroup.LayoutParams dialoglayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mdialog.addContentView(linview, dialoglayout);
        mdialog.setLeftBtn(
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        mdialog.setRightBtn(
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (!TextUtils.isEmpty(mdialog.getEditText())) {
                            editText.setText(mdialog.getEditText());
                        }
                        dialog.dismiss();
                    }
                });
        mdialog.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //一键诊断
            case R.id.nav_onekeydiagnosos:
                setTitle(R.string.onekeydiagnosos);
                ShowFragment(1);
                break;
            //设置
            case R.id.nav_setting:
                setTitle(R.string.setting);
                ShowFragment(2);
                break;
            //参数召测何设置
            case R.id.nav_measureandset:
                setTitle(R.string.measureandset);
                ShowFragment(3);
                break;
            //我的信息
            case R.id.nav_myinformation:
                CreateDialog();
                TabToast.makeText("功能暂未实现");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 指定显示某页
     * @param i
     */
    public void ShowFragment(int i) {
        //开启事务
        mFragmentTransaction = mFragmentManager.beginTransaction();
        //显示之前将所有的fragment都隐藏起来,在去显示我们想要显示的fragment
        hideFragment(mFragmentTransaction);
        switch (i) {
            //一键诊断
            case 1:
                if (home_1_fragment == null) {
                    home_1_fragment = new Home_1_Fragment();
                    mFragmentTransaction.add(R.id.main_view, home_1_fragment);
                } else {
                    mFragmentTransaction.show(home_1_fragment);
                }
                break;
            //设置
            case 2:
                if (home_2_fragment == null) {
                    home_2_fragment = new Home_2_Fragment();
                    mFragmentTransaction.add(R.id.main_view, home_2_fragment);
                } else {
                    mFragmentTransaction.show(home_2_fragment);
                }
                break;
            //参数召测何设置
            case 3:
                if (home_3_fragment == null) {
                    home_3_fragment = new Home_3_Fragment();
                    mFragmentTransaction.add(R.id.main_view, home_3_fragment);
                } else {
                    mFragmentTransaction.show(home_3_fragment);
                }
                break;
        }
        mFragmentTransaction.commit();
    }


    /**
     * 用来隐藏fragment的方法
     *
     * @param fragmentTransaction
     */
    private void hideFragment(FragmentTransaction fragmentTransaction) {
        //如果此fragment不为空的话就隐藏起来
        if (home_1_fragment != null) {
            fragmentTransaction.hide(home_1_fragment);
        }

        if (home_2_fragment != null) {
            fragmentTransaction.hide(home_2_fragment);
        }

        if (home_3_fragment != null) {
            fragmentTransaction.hide(home_3_fragment);
        }
    }

}
