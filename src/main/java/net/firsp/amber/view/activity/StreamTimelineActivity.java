package net.firsp.amber.view.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.firsp.amber.R;
import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.common.Notificator;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.util.UIHandler;
import net.firsp.amber.view.adapter.StatusListAdapter;
import net.firsp.amber.view.dialog.StatusDialogFragment;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;

public abstract class StreamTimelineActivity extends ActionBarActivity implements StatusListener {

    StatusListAdapter adapter;
    ListView view;

    Account account;
    TwitterStream userStream;
    TwitterStream filterStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Accounts.initialize(this);
        view = new ListView(this);
        setContentView(view);
        adapter = new StatusListAdapter(this);
        view.setAdapter(adapter);
        view.setOnItemClickListener((adapterView, view, i, l) -> {
            Status status = (Status) adapter.getItem(i);
            new StatusDialogFragment(this, status).show(getFragmentManager(), "Status");
        });
        view.setOnScrollListener(adapter);
        account = Accounts.getInstance().getAccount(getIntent().getLongExtra("id", 0));
        userStream = account.getTwitterStream();
        userStream.addListener(this);
        userStream.addListener(Notificator.initialize(this));
        filterStream = account.getTwitterStream();
        filterStream.addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Notificator.dispose();
        Crouton.cancelAllCroutons();
        userStream.shutdown();
        filterStream.shutdown();
    }

    public void addStatus(final Status status) {
        //表示位置がトップじゃなければTLに挿入しない
        if (view.getFirstVisiblePosition() != 0 || view.getChildAt(0) == null || view.getChildAt(0).getTop() != 0) {
            adapter.add(status);
            adapter.requireRefresh = true;
            return;
        }
        new UIHandler().post(()->adapter.addSorted(status));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.stream_action_tweet:
                DialogUtil.showTweetDialog(this, null);
                break;
            case R.id.stream_action_clear:
                adapter.clear(true);
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        running = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
    }

    boolean running;

    public boolean isRunning() {
        return running;
    }

    //StatusListener Implements

    @Override
    public void onStatus(Status status) {
        addStatus(status);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    }

    @Override
    public void onTrackLimitationNotice(int i) {
    }

    @Override
    public void onScrubGeo(long l, long l2) {
    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {
    }

    @Override
    public void onException(Exception e) {
    }
}
