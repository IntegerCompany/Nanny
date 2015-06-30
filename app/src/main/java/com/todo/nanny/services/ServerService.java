package com.todo.nanny.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.todo.nanny.ServerActivity;
import com.todo.nanny.audio.MediaStreamServer;
import com.todo.nanny.delegates.ServerDelegate;
import com.todo.nanny.simpleobject.SimpleObject;

import java.io.IOException;

public class ServerService extends Service {

    ServerBinder serverBinder = new ServerBinder();
    Connection serverConnection;
    Server server;
    Listener listener;
    MediaStreamServer mss;

    public final static int DATA_TRANSFER_PORT = 5679;

    private String ip;
    private int port;


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

    public void startServer(int port){
        mss = new MediaStreamServer(ServerService.this, port);
    }

    public void startObjectTransferingServer() {
        try {
            server = new Server();
            server.getKryo().register(SimpleObject.class);
            server.start();
            server.bind(port);
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
                    if (object instanceof SimpleObject){
                        Log.d("ServerService", "Yes! its Simple object with: "  + ((SimpleObject) object).getValue());

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

    public void initNetworkSettings(String ip, int port) {
        this.ip = ip;
        this.port = port;
        startObjectTransferingServer();
    }

    public void stopWorking() {
        server.stop();

        if(mss!=null) {
            mss.stop();
        }
    }

    public class ServerBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }

}
