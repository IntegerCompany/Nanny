package com.todo.nanny.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

import com.todo.nanny.ClientActivity;
import com.todo.nanny.audio.MediaStreamClient;

/**
 * Created by dmytro on 6/29/15.
 */
public class ClientService extends Service {
    final String TAG = "ClientService";
    int ip;
    Client client;
    Connection clientConnection;
    

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
        startDataTransferingClient();    

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

    public void startDataTransferingClient() {

        client = new Client();
        new Thread(client).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(5000, "192.168.1.167", ServerService.DATA_TRANSFER_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                clientConnection = connection;
                Log.d("ClientService", "Client: connected to server");
            }

            @Override
            public void received(Connection connection, Object object) {
                super.received(connection, object);
                clientConnection = connection;
                Log.d("ClientService", "Client: we have this object from server " + object.getClass().getName());
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                clientConnection = connection;
                Log.d("ClientService", "Client: we disconnected from server");
            }
        });


    }
    
}
