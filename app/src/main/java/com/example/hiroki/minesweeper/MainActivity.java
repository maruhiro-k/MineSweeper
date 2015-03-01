package com.example.hiroki.minesweeper;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private int level = 0;
    private int cols = 10;
    private int rows = 10;
    private int bombs = 10;

    private Tile tiles[][];

    private MineTimer timer = new MineTimer();

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
                r = r0 + dr;
                c = c0 + dc;
                if (++dc>1) {
                    dc = -1;
                    if (++dr>1) {
                        return false;
                    }
                }
                if (r>=0 && r<rows && c>=0 && c<cols) {
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

        // 設定読み込み
        loadSettings();

        // クリア
        reset();
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

        // 座標とって
        // フィールド内であること確認
        // フィールド内のRCに展開
        int c = -1, r = -1;

        Point pt = new Point();
        if (getTileIndex(event.getX(), event.getY(), pt)) {

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
                }
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    // タイマー
    // 表示変更

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // 難易度変更
        // ハイスコア
        // チュートリアル

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
        timer.init();

        // 盤面作り直し
        Tile.SIZE = 32;

        if (tiles==null || tiles.length!=rows || tiles[0].length!=cols) {
            tiles = new Tile[rows][];
        }
        for (int r=0; r<rows; ++r) {
            if (tiles[r]==null) {
                tiles[r] = new Tile[cols];
            }
            for (int c=0; c<cols; ++c) {
                if (tiles[r][c] == null) {
                    tiles[r][c] = new Tile(this);
                    tiles[r][c].setX(c * Tile.SIZE);
                    tiles[r][c].setY(r * Tile.SIZE);
                }
                tiles[r][c].clear();
            }
        }
        Log.i("tiles", tiles.length + "," + tiles[0].length);
    }

    private void clearDownTile() {
        // へこんでるタイルを全てもとに戻す
        for (int r=0; r<rows; ++r) {
            for (int c=0; c<cols; ++c) {
                if (tiles[r][c].get()==Tile.ST_DOWN) {
                    tiles[r][c].set(Tile.ST_UNKNOWN);
                }
            }
        }
    }

    private boolean getTileIndex(float x, float y, Point index) {
        // 座標からタイル番号を計算
        View field = findViewById(R.id.action_bar);
        x -= field.getX();
        y -= field.getY();
        float c = x / Tile.SIZE;
        float r = y / Tile.SIZE;
        if (c<0 || c>=cols || r<0 || r>=rows) {
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
        for (int b = bombs; b>0; ) {
            int r = rnd.nextInt(rows);
            int c = rnd.nextInt(cols);
            if (r!=start_r && c!=start_c && !tiles[r][c].isBomb()) {
                tiles[r][c].putBomb();
                --b;
            }
        }
    }

    private boolean isFlagTime() {
        return false;
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
        int remains = bombs;
        for (int rr=0; rr<rows; ++rr) {
            for (int cc = 0; cc <= cols; ++cc) {
                if (tiles[rr][cc].get()==Tile.ST_FLAG) {
                    --remains;
                }
            }
        }

        // todo 残り数描画
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
        int remains = bombs;
        for (int rr=0; rr<rows; ++rr) {
            for (int cc = 0; cc <= cols; ++cc) {
                st = tiles[rr][cc].get();
                if (st==Tile.ST_UNKNOWN || st==Tile.ST_FLAG) {
                    --remains;
                }
            }
        }
        return remains;
    }

    private boolean openTile(int r, int c) {
        // タイルを開く
        if (r < 0 || c < 0 || r >= rows || c >= cols) {
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
        tiles[it.r][it.c].set(Tile.ST_OPENED + bomb);

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
        // todo 全体をタッチできないように

        if (alive) {
            // リセットボタンを喜んだ絵に
        }
        else {
            // リセットボタンを悲しい絵に
        }

        // クリアなら残りの爆弾を旗に変換
        // ミスなら残りの爆弾表示
        for (int r=0; r<rows; ++r) {
            for (int c=0; c<cols; ++c) {
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

        // タイマーストップ
        timer.stop();
    }
}
