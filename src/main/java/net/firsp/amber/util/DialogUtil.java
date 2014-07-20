package net.firsp.amber.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;

import net.firsp.amber.account.Accounts;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.StatusUpdate;

public class DialogUtil {

    public static void showException(final Activity activity, Exception exception) {
        final StringBuilder sb = new StringBuilder();
        sb.append(exception.toString());
        sb.append("\n");
        for (StackTraceElement el : exception.getStackTrace()) {
            sb.append(el.toString());
            sb.append("\n");
        }
        new UIHandler(){
            @Override
            public void run(){
                new AlertDialog.Builder(activity).setMessage(sb.toString()).create().show();
            }
        };

    }

    public static ProgressDialog createProgress(Context context) {
        ProgressDialog d = new ProgressDialog(context);
        d.setMessage("Please wait");
        d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        d.setCancelable(false);
        return d;
    }

    //statusがnullならツイート、存在するならリプライ
    public static void showTweetDialog(final Activity activity, final Status status){
        final EditText editText = new EditText(activity);
        if(status != null){
            editText.setText("@" + status.getUser().getScreenName() + " ");
        }
        new AlertDialog.Builder(activity)
                .setTitle(status == null ? "Tweet" : "Reply")
                .setView(editText)
                .setPositiveButton("発射", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AsyncTwitter t = Accounts.getInstance().getDefaultAccount().getAsyncTwitter();
                        t.addListener(AsyncTwitterUtil.getTwitterListener(activity));
                        StatusUpdate s = new StatusUpdate(editText.getText().toString());
                        if(status != null){
                            s.setInReplyToStatusId(status.getId());
                        }
                        t.updateStatus(s);
                    }
                })
                .create()
                .show();
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }
}
