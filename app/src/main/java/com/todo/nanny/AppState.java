package com.todo.nanny;


import android.content.Context;
import android.content.SharedPreferences;

public class AppState {



    private static Context mContext;
    private static SharedPreferences sPrefLog;

    private static boolean isFirstLogin;


    public static boolean isFirstLogin(){
        if (sPrefLog.getBoolean("isFirstLogin", true)) return true;
        else {
            SharedPreferences.Editor logEditor = sPrefLog.edit();
            logEditor.putBoolean("isFirstLogin", false);
            logEditor.commit();
            return false;
        }
    }

    public static void setupAppState(Context context){
        mContext = context;
        sPrefLog = context.getSharedPreferences("egocentrum",Context.MODE_PRIVATE);

    }

}