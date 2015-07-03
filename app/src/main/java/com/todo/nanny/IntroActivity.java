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
        // NOTE: you will probably need to ask VIBRATE permission in Manifest
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed() {
        //temp intent
        if(AppState.isIntroShowing()) {
            startActivity(new Intent(this, LauncherActivity.class));
            AppState.introHasBeenShown();
        }
        finish();
    }

    @Override
    public void onDonePressed() {
       // temp intent
        if(AppState.isIntroShowing()) {
            startActivity(new Intent(this, LauncherActivity.class));
            AppState.introHasBeenShown();
        }
        finish();
    }
}
