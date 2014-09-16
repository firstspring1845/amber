package net.firsp.amber.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.util.UIHandler;

import twitter4j.Status;
import twitter4j.Twitter;

public class DigUpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo_Panel);

        Intent intent = getIntent();

        final ProgressDialog d = DialogUtil.createProgress(this);
        d.show();

        try {
            long id = Long.parseLong(intent.getData().toString().replaceAll(".*/", ""));
            Accounts.initialize(this);
            new Thread(() -> {
                try {
                    Twitter t = Accounts.getInstance().getDefaultAccount().getTwitter();
                    Status s = t.showStatus(id);
                    //とりあえず削除を試みる
                    try {
                        t.destroyStatus(s.getCurrentUserRetweetId());
                    } catch (Exception e) {
                    }
                    t.retweetStatus(id);
                } catch (Exception e) {
                    DialogUtil.showException(this, e);
                }
                new UIHandler().post(() -> {
                    d.dismiss();
                    finish();
                });
            }).start();
        } catch (Exception e) {
            finish();
        }
    }
}
