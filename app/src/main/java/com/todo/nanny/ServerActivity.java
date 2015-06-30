package com.todo.nanny;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.todo.nanny.audio.MediaStreamClient;
import com.todo.nanny.audio.MediaStreamServer;

public class ServerActivity extends Activity {
    TextView textView1;
    String ip;
    int port;
    MediaStreamServer mss;
    MediaStreamClient msc;

    FloatingActionButton button1;
    View start_view, new_view;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LayoutInflater inflater = getLayoutInflater();

        start_view = inflater.inflate(R.layout.server_show_id,null);
        new_view = inflater.inflate(R.layout.server_sleeping_baby,null);

        addView(start_view);


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

                replaceView(start_view,new_view);

                if (button1.getTitle().equals("Start")) {
                    button1.setTitle("Stop");
                    port = 54792;

                    textView1.append("Starting server\n");
                    mss = new MediaStreamServer(ServerActivity.this, port);

                } else if (button1.getTitle().equals("Stop")) {
                    button1.setTitle("Start");
                    if (mss != null) {
                        textView1.append("Stopping server\n");
                        mss.stop();
                    }
                }
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("tw.rascov.MediaStreamer.ERROR")) {
                    textView1.append("Error: " + intent.getStringExtra("msg") + "\n");
                    button1.setTitle("Start");
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("tw.rascov.MediaStreamer.ERROR");
        registerReceiver(receiver, filter);
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
}
