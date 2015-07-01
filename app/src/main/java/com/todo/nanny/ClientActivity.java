package com.todo.nanny;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.todo.nanny.audio.MediaStreamClient;
import com.todo.nanny.services.ClientService;
import com.todo.nanny.services.ServerService;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ClientActivity extends Activity {
    EditText editText1;
    Button button1;
    TextView textView1;
    String ip;
    MediaStreamClient msc;
    Context ctx;
    boolean bound = false;
    ServiceConnection sConn;
    Intent intent;
    String LOG_TAG = "ClientActivity";
    ClientService clientService;
    boolean isAlarm;

    Handler handler;

    @Override
    protected void onStart() {
        super.onStart();
        if (!isAlarm) startService(intent);
        bindService(intent, sConn, 0);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(isAlarm){
            new AlertDialog.Builder(this)
                    .setTitle("Alarm")
                    .setMessage("Do you want to hear your baby?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            clientService.letMeHearBaby();
                            clientService.setNoiseCounter(0);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            clientService.setNoiseCounter(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        isAlarm = false;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // initialize layout variables
        editText1 = (EditText) findViewById(R.id.editText1);
        button1 = (Button) findViewById(R.id.button1);
        textView1 = (TextView) findViewById(R.id.textView1);
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (textView1 != null) {
                    textView1.append("\t" + "Wifi signal: " + getWifiSignal());
                }
                handler.postDelayed(this, 3000);

            }
        });


        intent = new Intent(this,ClientService.class);


        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                clientService = ((ClientService.MyBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };


        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button1.getText().toString().equals("Start")) {
                    button1.setText("Stop");
                    ip = editText1.getText().toString();
                    textView1.append("Starting client, " + ip + ":" + ServerService.PORT + "\n");
                    intent.putExtra("ip", ip);
                    clientService.startClient(ip);
                }
                else if(button1.getText().toString().equals("Stop")) {


                    textView1.append("Stopping client\n");
                    clientService.stopClient();
                    clientService.setIsLoudMessageSent(false);

                }
            }
        });


        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("ClientActivity", "OnReceive: " + intent.getAction());
                if(intent.getAction().equals("tw.rascov.MediaStreamer.ERROR")) {
                    textView1.append("Error: " + intent.getStringExtra("msg") + "\n");
                    //button1.setText("Start");
                }else if(intent.getAction().equals("com.todo.nanny.alarm")){
                    Intent it = new Intent(getApplicationContext(),ClientActivity.class);
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(it);
                    isAlarm = true;
                }else if(intent.getAction().equals("com.todo.nanny.wrongIP")){

                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mWifi.isConnected()) {
                        Toast.makeText(getApplicationContext(), "Cant connect to this ip, check it!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Check your wifi connection!", Toast.LENGTH_SHORT).show();

                    }

                    button1.setText("Start");
                    Log.d("ClientActivity", "another attempt to edit ip");
                }else if(intent.getAction().equals("com.todo.nanny.reconnectError")){
                    button1.setText("Start");
                    //TODO alert
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("tw.rascov.MediaStreamer.ERROR");
        filter.addAction("com.todo.nanny.alarm");
        filter.addAction("com.todo.nanny.wrongIP");
        filter.addAction("com.todo.nanny.reconnectError");

        registerReceiver(receiver, filter);
    }

    private int getWifiSignal(){

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 6);
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
