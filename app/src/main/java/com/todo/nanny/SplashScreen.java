package com.todo.nanny;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

public class SplashScreen extends Activity{

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3100;
    private final int SPLASH_TADA_PAUSE = 600;

    /** Called when the activity is first created. */
    Handler handler, handler2;
    Runnable runnable, runnable2;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.splash_contaniner_cancel);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handler.removeCallbacks(runnable);
                handler2.removeCallbacks(runnable2);

                Intent mainIntent = new Intent(SplashScreen.this, LauncherActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        });
        animateText();
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreen.this, LauncherActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        };
        handler.postDelayed(runnable, SPLASH_DISPLAY_LENGTH);
    }

    private void animateText(){
        handler2 = new Handler();
        runnable2 = new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                YoYo.with(Techniques.Tada)
                        .duration(1000)
                        .withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                developmentCompanyTextAnim();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(findViewById(R.id.splash_chart_container));
            }
        };
        handler2.postDelayed(runnable2, SPLASH_TADA_PAUSE);

    }

    private void developmentCompanyTextAnim(){
        YoYo.with(Techniques.Hinge)
                .duration(1000)
                .playOn(findViewById(R.id.tv_devepment_company));
    }

}
