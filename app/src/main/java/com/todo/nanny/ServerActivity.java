package com.todo.nanny;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.todo.nanny.audio.MediaStreamClient;
import com.todo.nanny.audio.MediaStreamServer;
import com.todo.nanny.services.ClientService;
import com.todo.nanny.services.ServerService;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends Activity {
    private static final String LOG_TAG = "ServerActivity";
    EditText editText1;
    Button button1;
    SeekBar volume;
    TextView textView1;
    String ip;

    MediaStreamServer mss;
    MediaStreamClient msc;
    
    ServerService serverService;
    private boolean bound;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize layout variables
        editText1 = (EditText) findViewById(R.id.editText1);
        button1 = (Button) findViewById(R.id.button1);
        volume = (SeekBar) findViewById(R.id.volume);
        volume.setMax(100);
        volume.setProgress(100);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView1.append("Current IP: "+getLocalIpAddress()+"\n");

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button1.getText().toString().equals("Start")) {
                    button1.setText("Stop");
                    ip = editText1.getText().toString();

                    textView1.append("Starting server\n");

                    serverService.startServer();


                }
                else if(button1.getText().toString().equals("Stop")) {
                    button1.setText("Start");
                    serverService.stopWorking();

                }
            }
        });

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                float vol = (float)(arg0.getProgress())/(float)(arg0.getMax());
                if(msc!=null) msc.setVolume(vol, vol);
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {}
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("tw.rascov.MediaStreamer.ERROR")) {
                    textView1.append("Error: " + intent.getStringExtra("msg") + "\n");
                    button1.setText("Start");
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("tw.rascov.MediaStreamer.ERROR");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, ServerService.class), new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                serverService = ((ServerService.ServerBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException e) { e.printStackTrace(); }
        return null;
    }

}
