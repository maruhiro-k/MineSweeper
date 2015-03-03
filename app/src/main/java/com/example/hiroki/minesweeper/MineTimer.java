package com.example.hiroki.minesweeper;

/**
 * Created by Hiroki on 2015/03/02.
 */
public class MineTimer {
    public MineTimer() {
        init();
    }

    public void init() {
        started = false;
    }
    public void start() {
        started = true;
    }
    public void stop() {
        started = false;
    }
    public boolean isWorking() {
        return started;
    }

    private boolean started = false;
}
