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
    private long startT;    // 開始した時間
    private long currentMsec;     // 経過時間
    private Handler h;

    static public final long MAX_TIME = 999999;

    public MineTimer(TextView textView) {
        this.textView = textView;
        clear();
    }

    public void start() {
        h = new Handler();
        timer = new Timer();
        startT = System.currentTimeMillis();
        currentMsec = 0;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (startT>0) {
                            updateTime();
                        }
                   }
                });
            }
        }, 0, 10);
    }

    private void updateTime() {
        currentMsec = System.currentTimeMillis() - startT;
        currentMsec = Math.min(currentMsec, MineTimer.MAX_TIME);

        if (textView!=null) {
            String s = String.format("%03d", currentMsec/1000);
            if (!s.equals(textView.getText().toString())) {
                textView.setText(s);
            }
        }
    }

    public void stop() {
        updateTime();
        startT = 0;
        if (timer!=null) {
            timer.cancel();
            timer = null;
        }
    }

    public void clear() {
        stop();
        currentMsec = 0;
        this.textView.setText("000");
    }

    public boolean isWorking() {
        return (timer!=null);
    }

    public long getTime() {
        return currentMsec;
    }
}
