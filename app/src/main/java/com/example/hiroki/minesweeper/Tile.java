package com.example.hiroki.minesweeper;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Hiroki on 2015/03/01.
 */
public class Tile extends ImageView {

    private boolean bomb = false;
    private int state = 0;

    public static final int ST_UNKNOWN = 0;
    public static final int ST_FLAG = 1;
    public static final int ST_QUESTION = 2;    // 使ってない
    public static final int ST_OPENED = 3;
    public static final int ST_BOMB = 12;
    public static final int ST_FIRED = 13;
    public static final int ST_BADFLAG = 14;
    public static final int ST_DOWN = 15;
    private static Bitmap img[] = new Bitmap[ST_BADFLAG+1];

    public static int SIZE = 1;    // タイルの基本サイズ

    public static void setBitmap(Bitmap bmp) {
        int h = bmp.getHeight();
        for (int i=ST_UNKNOWN; i<=ST_BADFLAG; ++i) {
            img[i] = Bitmap.createBitmap(bmp, h*i, 0, h, h);
        }
    }

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
        if (state==ST_DOWN) {
            setImageBitmap(img[ST_OPENED]);
        }
        else {
            setImageBitmap(img[state]);
        }
    }

    public void putBomb() {
        bomb = true;
    }

    public boolean isBomb() {
        return bomb;
    }
}
