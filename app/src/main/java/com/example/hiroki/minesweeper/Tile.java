package com.example.hiroki.minesweeper;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Hiroki on 2015/03/01.
 */
public class Tile extends ImageView {

    private boolean bomb = false;
    private int state = 0;

    public static final int ST_UNKNOWN = 0;
    public static final int ST_FLAG = 1;
    public static final int ST_DOWN = 2;
    public static final int ST_OPENED = 3;
    public static final int ST_BOMB = 12;
    public static final int ST_FIRED = 13;
    public static final int ST_BADFLAG = 14;

    public static int SIZE = 32;    // タイルの基本サイズ

    public Tile(Context context) {
        super(context);
        clear();
    }

    public void clear() {
        bomb = false;
        set(ST_UNKNOWN);
    }

    public int get() {
        return state;
    }

    public void set(int state) {
        this.state = state;
        // todo 画像切り替え
    }

    public void putBomb() {
        bomb = true;
    }

    public boolean isBomb() {
        return bomb;
    }
}
