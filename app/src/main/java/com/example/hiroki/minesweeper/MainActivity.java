package com.example.hiroki.minesweeper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends ActionBarActivity {

    // レベル別設定
    private class Setting {
        public int cols;
        public int rows;
        public int bombs;
        Setting(int c, int r, int b) {
            cols = c;
            rows = r;
            bombs = b;
        }
    }
    private final Setting s[] = {
            new Setting(9, 9, 10),    // 初級
            new Setting(16, 16, 40),    // 中級
            new Setting(22, 22, 99)     // 上級
    };

    private int level = 0;  // 選択レベル
    private MineTimer timer;  // ゲームタイマー
    private Tile tiles[][]; // 配置されたタイル
    private GridLayout field;   // タイル置き場
    private Bitmap resetImg[] = new Bitmap[4];  // リセットボタンの顔
    private ImageButton resetBtn;   // リセットボタン
    private TextView bombText;  // 爆弾の残り数

   // 3x3のマスを順番に走査するためのクラス
    private class AroundIterator
    {
        private AroundIterator(int r, int c) {
            r0 = r;
            c0 = c;
            reset();
        }
        private void reset() {
            dr = -1;
            dc = -1;
            r = -1;
            c = -1;
        }
        private boolean next() {
            while (true) {
                if (dr>1) {
                    return false;
                }
                r = r0 + dr;
                c = c0 + dc;
                if (++dc>1) {
                    dc = -1;
                    ++dr;
                }
                if (r>=0 && r<s[level].rows && c>=0 && c<s[level].cols) {
                    return true;
                }
            }
        }
        private int r0, c0, dr, dc, r, c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Viewを拾っておく
        field = (GridLayout)findViewById(R.id.FieldTable);
        resetBtn = (ImageButton) findViewById(R.id.ResetButton);
        bombText = (TextView) findViewById(R.id.BombCounter);
        TextView timerView = (TextView) findViewById(R.id.TimeCounter);
        timer = new MineTimer(timerView);  // ゲームタイマー

        // 画像用意
        Bitmap resetBmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.reset);
        int h = resetBmp.getHeight();
        resetImg[0] = Bitmap.createBitmap(resetBmp, h*0, 0, h, h);
        resetImg[1] = Bitmap.createBitmap(resetBmp, h*1, 0, h, h);
        resetImg[2] = Bitmap.createBitmap(resetBmp, h*2, 0, h, h);
        resetImg[3] = Bitmap.createBitmap(resetBmp, h*3, 0, h, h);

        Tile.setBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.tile));

        // 設定読み込み
        loadSettings();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (tiles==null) {
            reset();
        }
    }

    // リセットボタンを押したとき
    public void onReset(View v) {
        // クリア
        reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // へこみを戻す
        clearDownTile();

        if (field.isEnabled()) {
            resetBtn.setImageBitmap(resetImg[0]);

            // 座標とってフィールド内であること確認
            // フィールド内のRCに展開
            Point pt = new Point();
            if (getTileIndex(event.getX(), event.getY(), pt)) {
                int c = pt.x, r = pt.y;

                // 旗立て
                if (isFlagTime()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        flag(r, c);
                        return true;
                    }
                }

                // 開く
                else {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        // 初回クリックでゲームスタート
                        if (isFirstClick()) {
                            startGame(r, c);
                        }

                        // 開ける
                        int res = open(r, c);
                        if (res < 0) {
                            end(false);     // 死んだ
                        } else if (res == 0) {
                            end(true);    // クリア
                        }
                    } else {
                        // へこます
                        down(r, c);
                        resetBtn.setImageBitmap(resetImg[1]);
                    }
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // レベル選択
        MenuItem item = menu.getItem(level);
        if (item!=null && !item.isChecked()) {
            item.setChecked(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_level0) {
            level = 0;
            reset();
            return true;
        }
        if (id == R.id.action_level1) {
            level = 1;
            reset();
            return true;
        }
        if (id == R.id.action_level2) {
            level = 2;
            reset();
            return true;
        }
        if (id == R.id.action_highscore) {
            // ハイスコア
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //! 設定読み込み
    private void loadSettings() {
        // 初期レベル
        // ハイスコア
    }

    // 盤面リセット
    private void reset() {
        // タイマー初期化
        timer.clear();
        bombText.setText(String.format("%03d", s[level].bombs));

        // 顔を戻す
        resetBtn.setImageBitmap(resetImg[0]);

        // サイズが違うなら盤面作り直し
        if (tiles!=null) {
            if (tiles.length!=s[level].rows || tiles[0].length!=s[level].cols) {
                tiles = null;
                /*
                for (int r=0; r<tiles.length; ++r) {
                    for (int c=0; c<tiles[r].length; ++c) {
                        tiles[r][c].remove
                    }
                }
                */
                field.removeAllViewsInLayout();
            }
        }

        // 中央に配置
        int w = field.getWidth();
        int h = field.getHeight();
        int length = Math.min(w, h);

        int sz = (length / s[level].cols / 4) * 4;
        Tile.SIZE = sz;

        field.setColumnCount(s[level].cols);
        field.setRowCount(s[level].rows);

        int dw = w - sz * s[level].cols;
        int dh = h - sz * s[level].rows;
        field.setPadding(dw/2, dh/8, dw-dw/2, dh-dh/8);

        // タイルを張り付ける
        if (tiles==null) {
            tiles = new Tile[s[level].rows][];
        }
        for (int r=0; r<s[level].rows; ++r) {
            if (tiles[r]==null) {
                tiles[r] = new Tile[s[level].cols];
            }
            for (int c=0; c<s[level].cols; ++c) {
                if (tiles[r][c] == null) {
                    tiles[r][c] = new Tile(this);
                    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                    param.width = Tile.SIZE;
                    param.height = Tile.SIZE;
                    param.columnSpec = GridLayout.spec(c);
                    param.rowSpec = GridLayout.spec(r);
                    tiles[r][c].setLayoutParams(param);
                    field.addView(tiles[r][c]);
                }
                tiles[r][c].clear();
            }
        }
        field.setEnabled(true);
    }

    private void clearDownTile() {
        // へこんでるタイルを全てもとに戻す
        for (int r=0; r<s[level].rows; ++r) {
            for (int c=0; c<s[level].cols; ++c) {
                if (tiles[r][c].get()==Tile.ST_DOWN) {
                    tiles[r][c].set(Tile.ST_UNKNOWN);
                }
            }
        }
    }

    private boolean getTileIndex(float x, float y, Point index) {
        // 座標からタイル番号を計算
        int pos[] = new int[2];
        field.getLocationInWindow(pos);
        x -= pos[0] + field.getPaddingLeft();
        y -= pos[1] + field.getPaddingTop();
        float c = x / Tile.SIZE;
        float r = y / Tile.SIZE;
        if (c<0 || c>=s[level].cols || r<0 || r>=s[level].rows) {
            return false;
        }
        index.set((int) (c), (int) (r));
        return true;
    }

    private boolean isFirstClick() {
        // タイマーが動いていなかったら最初のタッチ
        return (!timer.isWorking());
    }

    private void startGame(int start_r, int start_c) {
        // タイマー起動
        timer.start();

        // 爆弾配置
        Random rnd = new Random();
        for (int b = s[level].bombs; b>0; ) {
            int r = rnd.nextInt(s[level].rows);
            int c = rnd.nextInt(s[level].cols);
            if (r!=start_r && c!=start_c && !tiles[r][c].isBomb()) {
                tiles[r][c].putBomb();
                --b;
            }
        }
    }

    private boolean isFlagTime() {
        Switch s = (Switch)findViewById(R.id.FlagSwitch);
        return (s.isChecked());
    }

    private void flag(int r, int c) {
        // 旗立て
        if (tiles[r][c].get()==Tile.ST_UNKNOWN) {
            tiles[r][c].set(Tile.ST_FLAG);
        }
        else if (tiles[r][c].get()==Tile.ST_FLAG) {
            tiles[r][c].set(Tile.ST_UNKNOWN);
        }
        else {
            return; // すでに何かある
        }

        // 旗の数数える
        int remains = s[level].bombs;
        for (int rr=0; rr<s[level].rows; ++rr) {
            for (int cc=0; cc<s[level].cols; ++cc) {
                if (tiles[rr][cc].get()==Tile.ST_FLAG) {
                    --remains;
                }
            }
        }

        bombText.setText(String.format("%03d", remains));
    }

    private void down(int r, int c) {
        // へこます
        if (tiles[r][c].get()==Tile.ST_UNKNOWN) {
            tiles[r][c].set(Tile.ST_DOWN);
        }
        else {
            AroundIterator it = new AroundIterator(r, c);
            while (it.next()) {
                if (tiles[it.r][it.c].get()==Tile.ST_UNKNOWN) {
                    tiles[it.r][it.c].set(Tile.ST_DOWN);
                }
            }
        }
    }

    private int open(int r, int c) {
        boolean alive = true;

        int st = tiles[r][c].get();
        if (st==Tile.ST_UNKNOWN || st==Tile.ST_DOWN) {
            alive = openTile(r, c);
        }
        else {
            int num = st - Tile.ST_OPENED;
            if (num>0 && num<=8) {
                AroundIterator it = new AroundIterator(r, c);
                while (it.next()) {
                    if (tiles[it.r][it.c].get()==Tile.ST_FLAG) {
                        num--;
                    }
                }
                if (num <= 0) { // 数字以上の旗があるなら周囲を開ける
                    it.reset();
                    while (it.next()) {
                        alive &= openTile(it.r, it.c);
                    }
                }
            }
        }

        if (!alive) {
            return -1;  // 爆発した
        }

        // 残り数える
        int remains = 0;
        for (int rr=0; rr<s[level].rows; ++rr) {
            for (int cc=0; cc<s[level].cols; ++cc) {
                st = tiles[rr][cc].get();
                if (st==Tile.ST_UNKNOWN || st==Tile.ST_FLAG) {
                    ++remains;
                }
            }
        }
        return remains - s[level].bombs;
    }

    private boolean openTile(int r, int c) {
        // タイルを開く
        if (r < 0 || c < 0 || r >= s[level].rows || c >= s[level].cols) {
            return true;    // 場外
        }
        int st = tiles[r][c].get();
        if (st!=Tile.ST_UNKNOWN && st!=Tile.ST_DOWN) {
            return true;    // 開く必要なし
        }
        if (tiles[r][c].isBomb()) {
            tiles[r][c].set(Tile.ST_FIRED); // 爆弾踏んだ
            return false;
        }

        // 周りの爆弾数える
        int bomb = 0;
        AroundIterator it = new AroundIterator(r, c);
        while (it.next()) {
            if (tiles[it.r][it.c].isBomb()) {
                bomb++;
            }
        }
        tiles[r][c].set(Tile.ST_OPENED + bomb);

        // 0なら回りも開く
        if (bomb == 0) {
            it.reset();
            while (it.next()) {
                openTile(it.r, it.c);
            }
        }
        return true;
    }

    private void end(boolean alive) {
        // 終了
        field.setEnabled(false);

        if (alive) {
            resetBtn.setImageBitmap(resetImg[2]);   // リセットボタンを喜んだ絵に
        }
        else {
            resetBtn.setImageBitmap(resetImg[3]);   // リセットボタンを悲しい絵に
        }

        // クリアなら残りの爆弾を旗に変換
        // ミスなら残りの爆弾表示
        for (int r=0; r<s[level].rows; ++r) {
            for (int c=0; c<s[level].cols; ++c) {
                int st = tiles[r][c].get();
                if (tiles[r][c].isBomb()) {
                    if (st==Tile.ST_UNKNOWN) {
                        tiles[r][c].set(alive ? Tile.ST_FLAG : Tile.ST_BOMB);
                    }
                }
                else {
                    if (tiles[r][c].get()==Tile.ST_FLAG) {
                        tiles[r][c].set(Tile.ST_BADFLAG);
                    }
                }
            }
        }

        if (alive) {
            bombText.setText("000");
        }

        // タイマーストップ
        timer.stop();
    }
}
