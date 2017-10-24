package com.one.key.diagnosis.servlet;

import android.os.AsyncTask;

import com.one.key.diagnosis.activity.fragment.Fragment_1;
import com.one.key.diagnosis.utils.SendByteUtil;

/**
 * Created by wwwfa on 2017/10/24.
 */

public class Fragemt_1_Test_Servlet extends AsyncTask<String, Integer, Boolean> {
    private static final String TAG = "Fragemt_1_Test_Servlet";

    Fragment_1 fragment_1;

    public Fragemt_1_Test_Servlet(Fragment_1 fragment_1) {
        this.fragment_1 = fragment_1;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String ipcmd = SendByteUtil.getInstance().splitMessage("7B", "4513000300", "0A", "00004000", "");
        SendByteUtil.getInstance().sendByte(ipcmd);
        return SendByteUtil.getInstance().sendByte(ipcmd);
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        if (b)
            fragment_1.update();

    }
}
