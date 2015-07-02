package com.todo.nanny;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.TimeZone;

public class AppState {

    private static final String LOG_STATUS = "LogStatus" ;



    private static Context mContext;
    private static SharedPreferences sPrefLog;




    public static void setupAppState(Context context){
        mContext = context;
        sPrefLog = context.getSharedPreferences("egocentrum", Context.MODE_PRIVATE);



    }

    public static boolean isIntroShowing(){

        if (sPrefLog.getBoolean("firstrun", true)) {

            sPrefLog.edit().putBoolean("firstrun", false).apply();
            return true;
        }

        return false;
    }

    public static boolean isWifiConnected(){
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }
}
