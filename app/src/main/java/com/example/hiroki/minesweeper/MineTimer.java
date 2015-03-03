package com.example.hiroki.minesweeper;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hiroki on 2015/03/02.
 */
public class MineTimer {

    private TextView textView;
    private Timer timer;
    private long startT;
    private Handler h;

    public MineTimer(TextView textView) {
        this.textView = textView;
        clear();
    }

    public void start() {
        h = new Handler();
        timer = new Timer();
        startT = System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (startT>0) {
                            long sec = (System.currentTimeMillis() - startT) / 1000 + 1;
                            String s = String.format("%03d", sec);
                            if (!s.equals(textView.getText().toString())) {
                                textView.setText(s);
                            }
                        }
                   }
                });
            }
        }, 0, 10);
    }
    public void stop() {
        startT = 0;
        if (timer!=null) {
            timer.cancel();
            timer = null;
        }
    }
    public void clear() {
        stop();
        this.textView.setText("000");
    }
    public boolean isWorking() {
        return (timer!=null);
    }
}
