package com.todo.nanny;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.todo.nanny.intro.FirstSlide;
import com.todo.nanny.intro.SecondSlide;


public class IntroActivity extends AppIntro {

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(new FirstSlide(), getApplicationContext());
        addSlide(new SecondSlide(), getApplicationContext());

        // OPTIONAL METHODS
        // Override bar/separator color

        setBarColor(Color.parseColor("#78FFAEAE"));
        setSeparatorColor(Color.parseColor("#ffaeae"));

        // Hide Skip button
        showSkipButton(true);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed() {
        //temp intent
        startActivity(new Intent(this,ClientActivity.class));
        this.finish();
    }

    @Override
    public void onDonePressed() {
       // temp intent
        startActivity(new Intent(this,ServerActivity.class));
        this.finish();
    }
}
