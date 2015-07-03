package com.todo.nanny.helperclasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.todo.nanny.R;

import java.util.ArrayList;

/**
 * Created by dmytro on 7/3/15.
 */
public class VoiceTestDialog extends AlertDialog implements VolumeReceiver{

    TextView tvVolume;
    Button btnHide;



    public VoiceTestDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.volume_lvl_dialog);

        tvVolume = (TextView) findViewById(R.id.tv_dialog_volume_lvl);
        btnHide = (Button) findViewById(R.id.btn_dialog_hide);
        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    @Override
    public void onReceiveVolumeLevel(int vol) {
        if (tvVolume != null) {
            tvVolume.setText("" + vol);
            Log.d("VolumeTestDialog", ""+ vol);
        }
    }
}
