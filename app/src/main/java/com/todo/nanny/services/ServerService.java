package com.todo.nanny.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.todo.nanny.ServerActivity;
import com.todo.nanny.audio.MediaStreamServer;
import com.todo.nanny.delegates.ServerDelegate;
import com.todo.nanny.simpleobject.MessageSO;
import com.todo.nanny.simpleobject.SimpleObject;
import com.todo.nanny.simpleobject.VolumeSO;

import java.io.IOException;

public class ServerService extends Service {

    public static final int PORT = 54792;


    ServerBinder serverBinder = new ServerBinder();
    Connection serverConnection;
    Server server;
    Listener listener;
    MediaStreamServer mss;
    private MediaRecorder mRecorder = null;
    Handler handler;
    boolean endHandler = false;
    long serverStartTime;

    public ServerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serverBinder;
    }

    public void startServer(){

        startObjectTransferingServer();
        start();



        handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                if (!endHandler) {
                    int aml = getAmplitude();

                    if (aml > 100) {
                        sendAlarm(aml);
                    }
                    Log.d("ServerService", "" + aml);


                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(r, 1000);


    }

    public void startObjectTransferingServer() {
        try {
            server = new Server();
            server.getKryo().register(SimpleObject.class);
            server.getKryo().register(VolumeSO.class);
            server.getKryo().register(MessageSO.class);
            server.getKryo().register(Long.class);

            server.start();
            serverStartTime = System.currentTimeMillis();
            Log.d("ServerService", "Port: " + (PORT + 1));
            server.bind(PORT + 1);
            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                    serverConnection = connection;
                    serverConnection.sendTCP(serverStartTime);
                    Log.d("ServerService", "Server: Someone connected, sending server start time: " + serverStartTime);
                }

                @Override
                public void received(Connection connection, Object object) {
                    super.received(connection, object);
                    serverConnection = connection;
                    Log.d("ServerService", "Server: we have this object from client " + object.getClass().getName());
                    if (object instanceof SimpleObject) {
                        Log.d("ServerService", "Yes! its Simple object with: " + ((SimpleObject) object).getValue());
                        Intent it = new Intent(getApplicationContext(), ServerActivity.class);
//                        it.setComponent(new ComponentName(getApplicationContext().getPackageName(), ServerActivity.class.getName()));
                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(it);
                    } else if (object instanceof MessageSO) {
                        MessageSO message = (MessageSO) object;
                        int code = message.getCode();
                        switch (code){
                            case MessageSO.LET_ME_HEAR_BABY: {
                                startVoiceTransfering();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (serverConnection != null && serverConnection.isConnected()) {
                                            serverConnection.sendTCP(new MessageSO(2));
                                        }
                                    }
                                }).start();
                            }
                            break;
                            case MessageSO.START_SERVER_RECORDER: {
                                start();
                                break;
                            }
                        }
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    super.disconnected(connection);
                    serverConnection = connection;
                    Log.d("ServerService", "Server: Client disconnected");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startVoiceTransfering(){
        stop();
        mss = new MediaStreamServer(ServerService.this, PORT);
    }



    public void stopWorking() {
        if(mss!=null) {
            mss.stop();
        }
        //start();
    }

    public class ServerBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }

    public void sendAlarm(final int volume) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(serverConnection!= null && serverConnection.isConnected()){
                    serverConnection.sendTCP(new VolumeSO(volume));
                }
            }
        }).start();

    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public int getAmplitude() {
        if (mRecorder != null)
            return mRecorder.getMaxAmplitude();
        else
            return 0;

    }

    public void killAll(){
        if(mss!=null){
            mss.stop();
        }
        if(server != null){
            server.stop();
        }
        endHandler = true;
        stop();
    }

}
