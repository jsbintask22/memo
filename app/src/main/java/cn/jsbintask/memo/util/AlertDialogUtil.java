package cn.jsbintask.memo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import cn.jsbintask.memo.R;

/**
 * @author jsbintask@gmail.com
 * @date 2018/4/25 20:25
 */

public class AlertDialogUtil {
    public static void showDialog(Context context, int msg, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false)
                .setPositiveButton(R.string.confirm_text, positiveListener)
                .setNegativeButton(R.string.cancel_text, null);
        builder.create().show();
    }

    public static void showDialog(Context context, String msg, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false)
                .setPositiveButton(R.string.confirm_text, positiveListener)
                .setNegativeButton(R.string.cancel_text, null);
        builder.create().show();
    }

    public static AlertDialog showDialog(Context context, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        return builder.create();
    }
}
