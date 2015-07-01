package com.todo.nanny;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.todo.nanny.audio.MediaStreamClient;
import com.todo.nanny.audio.MediaStreamServer;
import com.todo.nanny.services.ClientService;
import com.todo.nanny.services.ServerService;

import java.io.IOException;
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

    FloatingActionButton button1;
    ImageButton imageButton;
    View start_view, new_view;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LayoutInflater inflater = getLayoutInflater();

        start_view = inflater.inflate(R.layout.server_show_id,null);
        new_view = inflater.inflate(R.layout.server_sleeping_baby,null);

        addView(start_view);


        imageButton = (ImageButton) findViewById(R.id.ear_sleeping_button);
        imageButton.setScaleX(0.01f);
        imageButton.setScaleY(0.01f);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        button1 = (FloatingActionButton) findViewById(R.id.ok_main_button);
        button1.setTitle("Start");



        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        textView1 = (TextView) findViewById(R.id.tv_ip_address);
        textView1.setText(ipAddress);

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button1.getText().toString().equals("Start")) {
                    button1.setText("Stop");
                    ip = editText1.getText().toString();

                replaceView(start_view,new_view);

                if (button1.getTitle().equals("Start")) {
                    button1.setTitle("Stop");
                    port = 54792;

                    textView1.append("Starting server\n");
                    serverService.startServer();



                } else if (button1.getText().toString().equals("Stop")) {
                    button1.setText("Start");
                    serverService.stopWorking();



                } else if (button1.getTitle().equals("Stop")) {
                    button1.setTitle("Start");
                    if (mss != null) {
                        textView1.append("Stopping server\n");
                        mss.stop();
                    }
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
                    //button1.setText("Start");
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

    public void addView(View v){
        ViewGroup parent = (ViewGroup) findViewById(R.id.container_main);
        parent.addView(v);
    }
    public void replaceView(View currentView, View newView) {
        ViewGroup parent = getMyParentView();
        if(parent == null) {
            return;
        }
        removeView(currentView);
        parent.addView(newView);

        button1.animate().translationY(400f);

        imageButton.setVisibility(View.VISIBLE);
        imageButton.animate().scaleX(1f);
        imageButton.animate().scaleY(1f);

        Log.i("Replacing", " View");
    }
    public ViewGroup getMyParentView() {
        return (ViewGroup) findViewById(R.id.container_main);
    }

    public void removeView(View view) {
        ViewGroup parent = getMyParentView();
        if(parent != null) {
            parent.removeView(view);
        }
    }



    @Override
    protected void onStop() {
        super.onStop();

    }

    public ServerService getServerService() {
        return serverService;
    }
}
