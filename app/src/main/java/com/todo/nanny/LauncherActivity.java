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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        AppState.setupAppState(this);

        isIntroMode();

        Button server = (Button) findViewById(R.id.btn_go_server);
        Button client = (Button) findViewById(R.id.btn_go_client);

        server.setOnClickListener(this);
        client.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_go_server:
                startActivity(new Intent(getApplication(),ServerActivity.class));
                finish();

                break;
            case R.id.btn_go_client:
                startActivity(new Intent(getApplication(),ClientActivity.class));


                break;
        }
    }

    private void isIntroMode(){
        if(AppState.isIntroShowing()){
            startActivity(new Intent(this,IntroActivity.class));
            this.finish();
        }

    }
}
