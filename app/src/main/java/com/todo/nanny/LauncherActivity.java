package com.todo.nanny;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;


public class LauncherActivity extends Activity implements View.OnClickListener {

    private MediaRecorder mRecorder = null;
    ProgressBar progressBar;
    int i  = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        isIntroMode();

        Button server = (Button) findViewById(R.id.btn_go_server);

        Button client = (Button) findViewById(R.id.btn_go_client);
        //Button getAmplitude = (Button) findViewById(R.id.btn_get_amplitude);
        progressBar = (ProgressBar) findViewById(R.id.pb_progress);

        final TextView lvl = (TextView) findViewById(R.id.tv_lelve);

        server.setOnClickListener(this);
        client.setOnClickListener(this);

        start();

        final Handler handler;

        handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                int aml = getAmplitude();
                lvl.setText("" + aml);

                if(aml < 5000 && aml > 2000 ){
                    lvl.setTextColor(Color.YELLOW);
                }else if(5000 <= aml && aml > 10000){
                    lvl.setTextColor(Color.RED);
                }else {
                    lvl.setTextColor(Color.GREEN);
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(r, 1000);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_go_server:
                startActivity(new Intent(getApplication(),ServerActivity.class));
                break;
            case R.id.btn_go_client:
                startActivity(new Intent(getApplication(),ClientActivity.class));
                break;
        }
    }
    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public int getAmplitude() {
        if (mRecorder != null)
            return mRecorder.getMaxAmplitude();
        else
            return 0;

    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    private void isIntroMode(){
        if(AppState.isIntroShowing()){
            startActivity(new Intent(this,IntroActivity.class));
            this.finish();
        }

    }
}
