package net.firsp.amber.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * Created by owner on 2014/07/06.
 */
public class DialogUtil {

    public static void showException(final Activity activity, Exception exception) {
        final StringBuilder sb = new StringBuilder();
        sb.append(exception.toString());
        sb.append("\n");
        for (StackTraceElement el : exception.getStackTrace()) {
            sb.append(el.toString());
            sb.append("\n");
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity).setMessage(sb.toString()).create().show();
            }
        });
    }

    public static ProgressDialog createProgress(Context context) {
        ProgressDialog d = new ProgressDialog(context);
        d.setMessage("Please wait");
        d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        d.setCancelable(false);
        return d;
    }
}
