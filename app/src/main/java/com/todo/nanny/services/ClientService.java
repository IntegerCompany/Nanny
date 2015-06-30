package com.todo.nanny.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.todo.nanny.ClientActivity;
import com.todo.nanny.audio.MediaStreamClient;

/**
 * Created by dmytro on 6/29/15.
 */
public class ClientService extends Service {

    final String TAG = "ClientService";

    MediaStreamClient msc;

    MyBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "MyService onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "MyService onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void startClient(String ip, int port){
        Log.d(TAG, "startClient");
        Log.d(TAG, String.valueOf(port));
        Log.d(TAG, ip);
        msc = new MediaStreamClient(ClientService.this, ip, port);
    }

    public void stopClient(){
        Log.d(TAG, "startClient");
        if (msc !=null){
            msc.stop();
        }
    }

    public class MyBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }
}
