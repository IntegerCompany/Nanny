package com.todo.nanny;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.todo.nanny.helperclasses.EpochTimer;
import com.todo.nanny.services.ClientService;

public class ClientActivity extends Activity {

    //Cry baby view
    ImageButton pauseBabyListening;
    ImageButton confirmVoiceTransfer;
    ImageButton muteBaby;


    MaterialEditText editText1;
    TextView textView1;
    String ip;
    Context ctx;
    boolean bound = false;
    ServiceConnection sConn;
    Intent intent;
    String LOG_TAG = "ClientActivity";
    ClientService clientService;
    boolean isAlarm;
    ImageButton ibtnStart;
    RelativeLayout containerSleep;
    RelativeLayout containerCry;
    BroadcastReceiver receiver;
    EpochTimer epochTimer;
    long timeServerStart = 0;

    OnClickListener oclStop;
    OnClickListener oclStart;

    Handler handler;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // initialize layout variables
        initViewsById();
        startSignalListener();


//        button1.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(button1.getText().toString().equals("Start")) {
//                    button1.setText("Stop");
//                    ip = editText1.getText().toString();
//                    textView1.append("Starting client, " + ip + ":" + ServerService.PORT + "\n");
//                    intent.putExtra("ip", ip);
//                    clientService.startClient(ip);
//                }
//                else if(button1.getText().toString().equals("Stop")) {
//
//
//                    textView1.append("Stopping client\n");
//                    clientService.stopClient();
//                   clientService.setIsLoudMessageSent(false);
//
//                }
//            }
//        });

        bindMyService();

        registerMyReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isAlarm) {
            startService(intent);
            bindService(intent, sConn, 0);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAlarm) {

            initCryBabyViews();
            showAlarmScreen();

        }
        isAlarm = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
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

    private void showAlarmScreen() {
        containerSleep.setVisibility(View.GONE);
        containerCry.setVisibility(View.VISIBLE);

    }
    private void showSleepingScreen(){
        //todo temp action
        containerSleep.setVisibility(View.VISIBLE);
        editText1.setVisibility(View.GONE);
        ibtnStart.setVisibility(View.GONE);
        containerCry.setVisibility(View.GONE);
    }
    private void confirmVoiceTransfer(){
        clientService.letMeHearBaby();
        clientService.setNoiseCounter(0);
        confirmVoiceTransfer.setOnClickListener(oclStop);
        confirmVoiceTransfer.setBackground(getResources().getDrawable(R.drawable.ear_with_shadow_green));
    }

    private void stopVoiceTransfer(){
        clientService.stopClient();
        clientService.setIsLoudMessageSent(false);
        confirmVoiceTransfer.setOnClickListener(oclStart);
        confirmVoiceTransfer.setBackground(getResources().getDrawable(R.drawable.ear_with_shadow));
        showSleepingScreen();

    }

    /**
     *  on pause listening
     */
    private void pauseListeningMyBaby(){
        clientService.setIsLoudMessageSent(true);
        pauseBabyListening.setOnClickListener(oclStop);
        pauseBabyListening.setBackground(getResources().getDrawable(R.drawable.start_with_shadow));
    }

    /**
     * on resume listening
     */
    private void resumeListeningMyBaby(){
        clientService.setIsLoudMessageSent(false);
        pauseBabyListening.setOnClickListener(oclStart);
        pauseBabyListening.setBackground(getResources().getDrawable(R.drawable.pause_with_shadow));
        clientService.setNoiseCounter(0);
    }
    private void initViewsById(){

        editText1 = (MaterialEditText) findViewById(R.id.et_enter_ip_here);
        ibtnStart = (ImageButton) findViewById(R.id.ibtn_start);
        epochTimer = new EpochTimer((TextView) findViewById(R.id.tvChronometer));
        epochTimer.start();
        if (timeServerStart != 0){
            epochTimer.setTimeStart(timeServerStart);
            epochTimer.start();
        }

        containerSleep = (RelativeLayout) findViewById(R.id.container_sleeping_baby_client);
        containerSleep.setVisibility(View.GONE);
        containerCry = (RelativeLayout) findViewById(R.id.container_crying_baby_client);
        containerCry.setVisibility(View.GONE);

        ibtnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = editText1.getText().toString();
                intent.putExtra("ip", ip);
                clientService.startClient(ip);
                Log.d("ClientActivity","Fucking start");
            }
        });

        oclStart = new OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()){
                    case R.id.ibtn_pause_baby_listening:
                        pauseListeningMyBaby();
                        break;
                    case R.id.ibtn_confirm_voice_transfer:
                        confirmVoiceTransfer();
                        break;
                }
            }
        };

        oclStop = new OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (view.getId()){
                    case R.id.ibtn_pause_baby_listening:
                        resumeListeningMyBaby();
                        break;
                    case R.id.ibtn_confirm_voice_transfer:
                        stopVoiceTransfer();
                        break;
                }
            }
        };

        pauseBabyListening = (ImageButton) findViewById(R.id.ibtn_pause_baby_listening);
        pauseBabyListening.setOnClickListener(oclStart);
    }
    private void wakeUpActivityAction(){
        Intent intent = new Intent(getApplication(),ClientActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void startSignalListener(){
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
    }

    private void initCryBabyViews(){

        confirmVoiceTransfer = (ImageButton) findViewById(R.id.ibtn_confirm_voice_transfer);
        confirmVoiceTransfer.setOnClickListener(oclStart);

        muteBaby = (ImageButton) findViewById(R.id.ibtn_mute_baby);
        muteBaby.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showSleepingScreen();
                clientService.setNoiseCounter(0);
                clientService.setIsLoudMessageSent(false);
            }
        });

    }

    private void bindMyService(){

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
    }

    private void registerMyReceiver(){

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("ClientActivity", "OnReceive: " + intent.getAction());
                if(intent.getAction().equals(getString(R.string.error_action))) {
                    //button1.setText("Start");
                }else if(intent.getAction().equals(getString(R.string.alarm_action))){

                    isAlarm = true;
                    wakeUpActivityAction();

                }else if(intent.getAction().equals(getString(R.string.wrong_ip_action))){

                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mWifi.isConnected()) {
                        Toast.makeText(getApplicationContext(), "Cant connect to this ip, check it!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Check your wifi connection!", Toast.LENGTH_SHORT).show();

                    }

                    //button1.setText("Start");
                    Log.d("ClientActivity", "another attempt to edit ip");
                }else if(intent.getAction().equals(getString(R.string.connection_error))){
                    //button1.setText("Start");
                    //TODO alert
                }else if(intent.getAction().equals(getString(R.string.show_sleeping_baby_screen_action))){
                    showSleepingScreen();
                }else if(intent.getAction().equals(getString(R.string.set_server_start_time_action))){
                    if (epochTimer != null){
                        timeServerStart = intent.getLongExtra("serverStartTime", System.currentTimeMillis());
                        epochTimer.setTimeStart(timeServerStart);
                        epochTimer.start();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("tw.rascov.MediaStreamer.ERROR");
        filter.addAction("com.todo.nanny.alarm");
        filter.addAction("com.todo.nanny.wrongIP");
        filter.addAction("com.todo.nanny.reconnectError");
        filter.addAction("com.todo.nanny.hide");
        filter.addAction("com.todo.nanny.serverStartTime");

        registerReceiver(receiver, filter);
    }



}
