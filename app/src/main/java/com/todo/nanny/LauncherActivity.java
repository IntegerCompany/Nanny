package com.todo.nanny;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.todo.nanny.helperclasses.VoiceTestDialog;

import java.io.IOException;


public class LauncherActivity extends Activity implements View.OnClickListener {

    AlertDialog soundDialog = null;
    MediaPlayer mp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        AppState.setupAppState(this);

        // TODO modify introActivity
        isIntroMode();

        Button server = (Button) findViewById(R.id.btn_go_server);
        Button client = (Button) findViewById(R.id.btn_go_client);

        server.setOnClickListener(this);
        client.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_go_server:

                startActivity(new Intent(getApplication(), ServerActivity.class));

                finish();
                break;

            case R.id.btn_go_client:

                startActivity(new Intent(getApplication(), ClientActivity.class));

                finish();
                break;
        }
    }

    private void isIntroMode() {
        if (AppState.isIntroShowing()) {
            startActivity(new Intent(this, IntroActivity.class));
            this.finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:


                // Creating and Building the Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select The Alert Sound");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        soundDialog.dismiss();
                        if (mp != null){
                            mp.stop();
                        }
                    }
                });
                builder.setSingleChoiceItems(new String[] {"AlertSound 1" , "AlertSound 2"},
                        -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch(item)
                        {
                            case 0:
                                if (mp != null){
                                    mp.stop();
                                }
                                mp = MediaPlayer.create(getApplicationContext(), R.raw.alert_sound);
                                mp.start();
                                AppState.setCurrentSound(R.raw.alert_sound);
                                break;
                            case 1:
                                if (mp != null){
                                    mp.stop();
                                }
                                mp = MediaPlayer.create(getApplicationContext(), R.raw.alert_sound_2);
                                mp.start();
                                AppState.setCurrentSound(R.raw.alert_sound_2);
                                break;

                        }
                    }
                });
                soundDialog = builder.create();
                soundDialog.show();

                break;
        }

        return super.onOptionsItemSelected(item);


    }
}
