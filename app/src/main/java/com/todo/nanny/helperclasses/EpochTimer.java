package com.todo.nanny.helperclasses;

import android.os.Handler;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EpochTimer{

        private Handler handler;
        private long timeStart = System.currentTimeMillis();
        private TextView tvChronometer;
        private boolean running;
        private Date dateToShow;
        private SimpleDateFormat dateFormat;


        public EpochTimer(TextView tvChronometer){
            this.tvChronometer = tvChronometer;
            handler = new Handler();
            dateToShow = new Date(System.currentTimeMillis());
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


        }

        public void start(){
            if (!running) {
                running = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (running) {
                            dateToShow.setTime(System.currentTimeMillis() - timeStart);
                            tvChronometer.setText(dateFormat.format(dateToShow));

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