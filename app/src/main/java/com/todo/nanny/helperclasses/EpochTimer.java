package com.todo.nanny.helperclasses;

import android.os.Handler;
import android.widget.TextView;

import java.util.Date;

public class EpochTimer{

        private Handler handler;
        private long timeStart = System.currentTimeMillis();
        private TextView tvChronometer;
        private boolean running;
        private Date dateToShow;


        public EpochTimer(TextView tvChronometer){
            this.tvChronometer = tvChronometer;
            handler = new Handler();
            dateToShow = new Date(System.currentTimeMillis());


        }

        public void start(){
            if (!running) {
                running = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (running) {
                            dateToShow.setTime(System.currentTimeMillis() - timeStart);
                            tvChronometer.setText(dateToShow.getHours() + ":" + dateToShow.getMinutes() + ":" + dateToShow.getSeconds());

                            handler.postDelayed(this, 1000);
                        }
                    }
                });
            }
            running = true;
        }

        public void stop(){
            running = false;
        }

        public long getTimeStart() {
            return timeStart;
        }

        public void setTimeStart(long timeStart) {
            this.timeStart = timeStart;
        }


    }