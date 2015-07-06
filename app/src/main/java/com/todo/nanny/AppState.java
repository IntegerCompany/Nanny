package com.todo.nanny;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.TimeZone;

public class AppState {

    private static final String LOG_STATUS = "LogStatus";

    private static Context mContext;
    private static SharedPreferences sPrefLog;

    public static void setupAppState(Context context) {
        mContext = context;
        sPrefLog = context.getSharedPreferences("nanny", Context.MODE_PRIVATE);
    }

    public static boolean isIntroShowing() {
        return sPrefLog.getBoolean("firstrun", true);
    }

    public static void introHasBeenShown() {

        sPrefLog.edit().putBoolean("firstrun", false).apply();
    }

    public static void setSeekBarProgress(int progress) {
        sPrefLog.edit().putInt("progress", progress).apply();
    }

    public static int getSeekBarProgress() {
        return sPrefLog.getInt("progress", 1500);
    }

    public static void setIP(String ip) {
        sPrefLog.edit().putString("ip", ip).apply();
    }

    public static String getIP() {
        return sPrefLog.getString("ip", "");
    }

    public static boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }
}

