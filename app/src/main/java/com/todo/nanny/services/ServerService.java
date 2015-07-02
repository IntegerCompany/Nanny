package com.todo.nanny.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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
    boolean isRecorderBroken = false;
    boolean isVoiceTransfer = false;
    int aml;
    int counter;

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
                    if(aml == 0){
                        counter ++;
                    }
                    if(aml>0){
                        isRecorderBroken = false;
                    }
                    getAmplitude();
                    if(isRecorderBroken && !isVoiceTransfer){
                        start();
                    }
                    if(!isVoiceTransfer && counter > 5){
                        counter = 0;
                        isRecorderBroken = true;
                    }
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

            server.start();
            Log.d("ServerService", "Port: " + (PORT + 1));
            server.bind(PORT + 1);
            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    super.connected(connection);
                    serverConnection = connection;
                    Log.d("ServerService", "Server: Someone connected");
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
                                isVoiceTransfer = true;
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
                                stopWorking();

                                break;
                            }
                        }
                    }
                }

                @Override
                public void disconnected(Connection connection) {
                    super.disconnected(connection);
                    serverConnection = connection;
                    isVoiceTransfer = false;
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
            Log.d("ServerService", "Voice Stopped");
        }
        start();
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
             stop();
            try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
            isRecorderBroken = false;
            } catch (IllegalStateException e) {
                Log.d("ServerService", "Can't start");
                isRecorderBroken = true;
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("ServerService", "Started");
        }



    public void stop() {
        if (mRecorder != null) {
            if (isRecorderBroken || isVoiceTransfer) {
                try {
                    mRecorder.stop();
                    mRecorder.reset();
                    mRecorder.release();
                    mRecorder = null;
                    isRecorderBroken = false;
                    isVoiceTransfer = false;
                    Log.d("ServerService", "Stopped");
                } catch (IllegalStateException e) {
                    Log.d("ServerService", "Can't stop");
                }
            }
        }
    }

    public void getAmplitude() {
        aml = 0;
        if (mRecorder != null){
            try{
                aml = mRecorder.getMaxAmplitude();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
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
