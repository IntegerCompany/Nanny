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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.todo.nanny.audio.MediaStreamClient;
import com.todo.nanny.audio.MediaStreamServer;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends Activity {
    TextView textView1;
    String ip;
    int port;
    MediaStreamServer mss;
    MediaStreamClient msc;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final FloatingActionButton button1 = (FloatingActionButton) findViewById(R.id.button1);
        button1.setTitle("Start");

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        textView1 = (TextView) findViewById(R.id.tv_ip_address);
        textView1.setText(ipAddress);

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
}
