package com.example.hiroki.minesweeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

/**
 * Created by Hiroki on 2015/03/15.
 */
public class ScoreDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity act = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.high_score);

        // スコア表示
        String s = new String();
        Resources rsc = getResources();
        SharedPreferences pref = act.getPreferences(Context.MODE_PRIVATE);
        s += rsc.getString(R.string.level0) + "\t\t" + String.format("%.02f 秒", pref.getLong("score0", MineTimer.MAX_TIME)/1000.f);
        s += "\n\n";
        s += rsc.getString(R.string.level1) + "\t\t" + String.format("%.02f 秒", pref.getLong("score1", MineTimer.MAX_TIME)/1000.f);
        s += "\n\n";
        s += rsc.getString(R.string.level2) + "\t\t" + String.format("%.02f 秒", pref.getLong("score2", MineTimer.MAX_TIME)/1000.f);
        builder.setMessage(s);

        // 記録消去
        builder.setNegativeButton(R.string.clear_score, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Activity act = getActivity();
                SharedPreferences pref = act.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putLong("score0", MineTimer.MAX_TIME);
                editor.putLong("score1", MineTimer.MAX_TIME);
                editor.putLong("score2", MineTimer.MAX_TIME);
                editor.commit();
                dialog.dismiss();
            }
        });
        // 閉じる
        builder.setPositiveButton(R.string.close_score, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
