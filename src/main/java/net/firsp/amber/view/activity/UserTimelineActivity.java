package net.firsp.amber.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.view.adapter.StatusListAdapter;
import net.firsp.amber.view.dialog.StatusDialogFragment;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;

public class UserTimelineActivity extends Activity {

    StatusListAdapter adapter;
    Account account;
    String screenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Accounts.initialize(this);
        ListView v = new ListView(this);
        adapter = new StatusListAdapter(this);
        v.setAdapter(adapter);
        v.setOnScrollListener(adapter);
        v.setOnItemClickListener((adapterView, view, i, l) -> {
            Status status = (Status) adapter.getItem(i);
            new StatusDialogFragment(this, status).show(getFragmentManager(), "Status");
        });
        setContentView(v);

        Intent intent = getIntent();

        //インテント経由の呼び出しならUri拾って正規表現で適当に抽出
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            Matcher m = Pattern.compile("twitter.com/[^/]*").matcher(uri.toString());
            if (m.find()) {
                screenName = m.group(0).split("/")[1];
            } else {
                CroutonUtil.error(this);
                return;
            }
        } else {
            screenName = intent.getStringExtra("screen_name");
        }
        setTitle(screenName);
        account = Accounts.getInstance().getDefaultAccount();

        new Thread(() -> {
            try {
                Twitter twitter = account.getTwitter();
                List<Status> list = twitter.getUserTimeline(screenName, new Paging(1, 200));
                for (Status status : list) {
                    adapter.add(status);
                }
                adapter.refresh();
            } catch (Exception e) {
                CroutonUtil.error(this);
            }
        }).start();
    }
}
