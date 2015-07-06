package com.todo.nanny;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.todo.nanny.helperclasses.VoiceTestDialog;
import com.todo.nanny.helperclasses.VolumeReceiver;
import com.todo.nanny.services.ServerService;

public class ServerActivity extends Activity {
    private static final String LOG_TAG = "ServerActivity";

    TextView textView1;
    String ip;

    ServiceConnection serviceConnection;


    ServerService serverService;
    boolean bound;

    FloatingActionButton button1;
    ImageButton imageButton;
    View start_view, new_view;
    Button btnCheckWifiSettings;

    BroadcastReceiver receiver;
    VoiceTestDialog voiceTestDialog;

    Button btnTestVolume,btnHelp;
    private boolean doubleBackToExitPressedOnce;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);


        LayoutInflater inflater = getLayoutInflater();

         voiceTestDialog = new VoiceTestDialog(this);


        start_view = inflater.inflate(R.layout.server_show_id, null);
        new_view = inflater.inflate(R.layout.server_sleeping_baby, null);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        button1 = (FloatingActionButton) findViewById(R.id.ok_main_button);
        button1.setTitle("Start");


        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        textView1 = (TextView) findViewById(R.id.tv_ip_address);
        textView1.setText(ipAddress);
        btnCheckWifiSettings = (Button) findViewById(R.id.btnCheckWifiSettings);
        btnCheckWifiSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        btnCheckWifiSettings.setVisibility(View.GONE);

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                addView(new_view);

                if (button1.getTitle().equals("Start")) {
                    button1.setTitle("Stop");
                    ip = textView1.getText().toString();

                    textView1.append("Starting server\n");
                    serverService.firstStart();
                    serverService.startServer();


                } else if (button1.getTitle().equals("Stop")) {
                    button1.setTitle("Start");
                    serverService.stopWorking();
                }
            }

        });

        btnHelp = (Button) findViewById(R.id.btn_server_help);
        btnHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ServerActivity.this, IntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("tw.rascov.MediaStreamer.ERROR")) {
                    textView1.append("Error: " + intent.getStringExtra("msg") + "\n");
                    //button1.setText("Start");
                }
                if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")){
                    checkAndHandleWifiState();
                }else if(intent.getAction().equals(context.getString(R.string.testing_voice_action))) {
                    VolumeReceiver volumeReceiver = voiceTestDialog;
                    volumeReceiver.onReceiveVolumeLevel(intent.getIntExtra("volume", 0));
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("tw.rascov.MediaStreamer.ERROR");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction(getApplicationContext().getString(R.string.testing_voice_action));
        registerReceiver(receiver, filter);
        checkAndHandleWifiState();
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
        bindService(new Intent(this, ServerService.class), serviceConnection, BIND_AUTO_CREATE);
        checkAndHandleWifiState();



    }

    public void addView(View newView) {
        ViewGroup parent = getMyParentView();
        if (parent == null) {
            return;
        }
        start_view.setVisibility(View.GONE);
        parent.addView(newView);

        button1.animate().translationY(400f);

        imageButton = (ImageButton) findViewById(R.id.ear_sleeping_button);
        imageButton.setScaleX(0.01f);
        imageButton.setScaleY(0.01f);
        imageButton.setVisibility(View.VISIBLE);
        imageButton.animate().scaleX(1f);
        imageButton.animate().scaleY(1f);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplication(), LauncherActivity.class));
                finish();
            }
        });
        Log.i("Replacing", " View");

        btnTestVolume = (Button) findViewById(R.id.btn_test_volume);
        btnTestVolume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceTestDialog.setCanceledOnTouchOutside(false);
                voiceTestDialog.show();
            }
        });


    }

    public ViewGroup getMyParentView() {
        return (ViewGroup) findViewById(R.id.container_server_main);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkAndHandleWifiState();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        serverService.stop();
        serverService.killAll();
        unbindService(serviceConnection);
    }

    public void onBackPressed() {
        Log.d("ServerActivity", "onBackPressed");
        if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(getApplication(),LauncherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }



    private void checkAndHandleWifiState() {
        if (textView1 != null & btnCheckWifiSettings != null & button1 != null) {
            if (AppState.isWifiConnected()) {
                textView1.setVisibility(View.VISIBLE);
                btnCheckWifiSettings.setVisibility(View.GONE);
                button1.setClickable(true);
            } else {
                textView1.setVisibility(View.GONE);
                btnCheckWifiSettings.setVisibility(View.VISIBLE);
                button1.setClickable(false);
            }
        }
    }


}
