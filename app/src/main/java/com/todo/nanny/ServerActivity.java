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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
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

    TextView textView1;
    String ip;
    BroadcastReceiver receiver;
    ServiceConnection serviceConnection;


    ServerService serverService;
    boolean bound;

    FloatingActionButton button1;
    ImageButton imageButton;
    View start_view, new_view;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LayoutInflater inflater = getLayoutInflater();

        start_view = inflater.inflate(R.layout.server_show_id, null);
        new_view = inflater.inflate(R.layout.server_sleeping_baby, null);

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

                replaceView(start_view, new_view);

                if (button1.getTitle().equals("Start")) {
                    button1.setTitle("Stop");
                    ip = textView1.getText().toString();

                    textView1.append("Starting server\n");
                    serverService.startServer();
                } else if (button1.getTitle().equals("Stop")) {
                    button1.setTitle("Start");
                    serverService.stopWorking();
                }
            }

        });

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("tw.rascov.MediaStreamer.ERROR")) {
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
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                serverService = ((ServerService.ServerBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };
        bindService(new Intent(this, ServerService.class),serviceConnection , BIND_AUTO_CREATE);
    }

    public void addView(View v) {
        ViewGroup parent = (ViewGroup) findViewById(R.id.container_main);
        parent.addView(v);
    }

    public void replaceView(View currentView, View newView) {
        ViewGroup parent = getMyParentView();
        if (parent == null) {
            return;
        }
        removeView(currentView);
        parent.addView(newView);

        button1.animate().translationY(400f);

        imageButton.setVisibility(View.VISIBLE);
        imageButton.animate().scaleX(1f);
        imageButton.animate().scaleY(1f);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplication(),LauncherActivity.class));
                finish();

            }
        });

        Log.i("Replacing", " View");
    }

    public ViewGroup getMyParentView() {
        return (ViewGroup) findViewById(R.id.container_main);
    }

    public void removeView(View view) {
        ViewGroup parent = getMyParentView();
        if (parent != null) {
            parent.removeView(view);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        serverService.killAll();
        unbindService(serviceConnection);
    }

    @Override
    public void onBackPressed() {
        Log.d("ServerActivity","onBackPressed");
        Intent intent = new Intent(getApplication(),LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
