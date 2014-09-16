package net.firsp.amber.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.EditText;

import net.firsp.amber.account.Accounts;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

public class DialogUtil {

    public static void showException(Activity activity, Exception exception) {
        final StringBuilder sb = new StringBuilder();
        sb.append(exception.toString());
        sb.append("\n");
        for (StackTraceElement el : exception.getStackTrace()) {
            sb.append(el.toString());
            sb.append("\n");
        }
        new UIHandler().post(() -> {
            new AlertDialog.Builder(activity).setMessage(sb.toString()).create().show();
        });
    }

    public static ProgressDialog createProgress(Context context) {
        ProgressDialog d = new ProgressDialog(context);
        d.setMessage("Please wait");
        d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        d.setCancelable(false);
        return d;
    }

    //statusがnullならツイート、存在するならリプライ
    public static void showTweetDialog(Activity activity, Status status) {
        EditText editText = new EditText(activity);
        if (status != null) {
            editText.setText("@" + status.getUser().getScreenName() + " ");
        }
        new AlertDialog.Builder(activity)
                .setTitle(status == null ? "Tweet" : "Reply")
                .setView(editText)
                .setPositiveButton("発射", (di, i) -> {
                    Twitter t = Accounts.getInstance().getDefaultAccount().getTwitter();
                    StatusUpdate s = new StatusUpdate(editText.getText().toString());
                    if (status != null) {
                        s.setInReplyToStatusId(status.getId());
                    }
                    new Thread(()->{
                       try{
                           t.updateStatus(s);
                           CroutonUtil.showText(activity, "ツイートしたんたん");
                       }catch(Exception e){
                           CroutonUtil.error(activity);
                       }
                    }).start();
                })
                .create()
                .show();
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

}
