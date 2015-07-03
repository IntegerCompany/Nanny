package com.todo.nanny.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.todo.nanny.AppState;
import com.todo.nanny.ClientActivity;

import com.todo.nanny.audio.MediaStreamClient;
import com.todo.nanny.simpleobject.MessageSO;
import com.todo.nanny.simpleobject.SimpleObject;
import com.todo.nanny.simpleobject.VolumeSO;

import java.io.IOException;

/**
 * Created by dmytro on 6/29/15.
 */
public class ClientService extends Service {
    final String TAG = "ClientService";
    Client client;
    Connection clientConnection;
    boolean isLoudMessageSent, isReconnect, isExit = false;
    String ip;

    Handler handler;
    private static final int RECONNECTION_TIME = 10000;
    int reconnectionAttempt = 0;

    int maxVolume = 20000;


    //Used to count voice volume from server
    int noiseCounter = 0, volume = 0;


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
        handler = new Handler();
        Log.d(TAG, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        Log.v(TAG, "onStartCommand()");
        if (intent == null) {
            Log.w(TAG, "Service was stopped and automatically restarted by the system. Stopping self now.");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isExit = true;
        Log.d(TAG, "onDestroy");
    }

    public void startClient(String ip) {
        this.ip = ip;
        Log.d(TAG, "startClient");
        Log.d(TAG, String.valueOf(ServerService.PORT));
        Log.d(TAG, ip);
        startDataTransferingClient(ip);
    }

    public void stopClient() {
        Log.d(TAG, "stopClient");
        if (msc != null) {
            msc.stop();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (clientConnection != null && clientConnection.isConnected()) {
                    clientConnection.sendTCP(new MessageSO(3));
                }
            }
        }).start();
    }

    public class MyBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }

    public void startDataTransferingClient(final String ip) {
        client = new Client();
        client.getKryo().register(SimpleObject.class);
        client.getKryo().register(VolumeSO.class);
        client.getKryo().register(MessageSO.class);
        client.getKryo().register(Long.class);

        new Thread(client).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(5000, ip, ServerService.PORT + 1);
                } catch (IOException e) {
                    Log.d("ClientService", "cant connect to this ip: " + ip);
                    Intent intent = new Intent().setAction("com.todo.nanny.wrongIP");
                    if (!isReconnect) {
                        getApplicationContext().sendBroadcast(intent);
                    }

                }
            }
        }).start();

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                clientConnection = connection;
                Intent intent = new Intent().setAction("com.todo.nanny.hide");
                getApplicationContext().sendBroadcast(intent);
                Log.d("ClientService", "Client: connected to server");
            }

            @Override
            public void received(Connection connection, Object object) {
                super.received(connection, object);
                clientConnection = connection;
                Log.d("ClientService", "Client: we have this object from server " + object.getClass().getName());
                if (object instanceof VolumeSO) {
                    VolumeSO volumeSO = (VolumeSO) object;
                    Log.d("ClientService", "Volume: " + volumeSO.getVolume());
                    volume = volumeSO.getVolume();
                    if (volume > maxVolume) {
                        setNoiseCounter(getNoiseCounter() + 1);
                        Log.d("ClientService", "" + noiseCounter);
                    }

                }
                if (object instanceof MessageSO) {
                    MessageSO messageSO = (MessageSO) object;
                    switch (messageSO.getCode()) {
                        case MessageSO.READY_FOR_RECEIVING_VOICE:
                            startVoiceReceiving();
                            break;
                    }
                }
                if (object instanceof Long) {
                    long serverStartTime = (long) object;
                    Intent intent = new Intent("com.todo.nanny.serverStartTime");
                    intent.putExtra("serverStartTime", serverStartTime);
                    Log.d("ClientService", "Server start time = " + serverStartTime + ", current time: " + System.currentTimeMillis());
                    getApplicationContext().sendBroadcast(intent);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                clientConnection = connection;

                handler.post(new Runnable() {
                    public void run() {
                        if (!isExit) {
                            if (reconnectionAttempt < 3) {
                                startDataTransferingClient(ip);
                                reconnectionAttempt++;
                                handler.postDelayed(this, RECONNECTION_TIME);
                                isReconnect = true;
                            } else {
                                Intent intent = new Intent().setAction("com.todo.nanny.reconnectError");
                                getApplicationContext().sendBroadcast(intent);
                                isReconnect = false;
                                Log.d("ClientService", "Cant Connect to server!!!");
                                stopSelf();
                            }
                        }

                    }
                });
                Log.d("ClientService", "Client: we disconnected from server");
            }
        });


    }

    public void letMeHearBaby() {
        final MessageSO messageSO = new MessageSO();
        messageSO.setCode(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (clientConnection != null && clientConnection.isConnected()) {
                    clientConnection.sendTCP(messageSO);
                }
            }
        }).start();
    }

    public void startVoiceReceiving() {
        msc = new MediaStreamClient(ClientService.this, ip, ServerService.PORT);
    }

    public void setIsLoudMessageSent(boolean isLoudMessageSent) {
        this.isLoudMessageSent = isLoudMessageSent;
    }

    public int getNoiseCounter() {
        return noiseCounter;
    }

    public void setNoiseCounter(int noiseCounter) {
        this.noiseCounter = noiseCounter;
        if (noiseCounter > 2) {

            if (!isLoudMessageSent) {
                isLoudMessageSent = true;
                Intent intent = new Intent().setAction("com.todo.nanny.alarm");
                getApplicationContext().sendBroadcast(intent);
            } else {
                setNoiseCounter(0);
            }
        }
    }

    public void stopDataTransfering(){
        if(clientConnection != null){
            clientConnection.close();
        }
        client.stop();
        if (msc != null) {
            msc.stop();
        }
        isExit = true;
    }

    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
    }

}
