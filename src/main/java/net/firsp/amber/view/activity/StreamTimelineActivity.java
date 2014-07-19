package net.firsp.amber.view.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.firsp.amber.R;
import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.util.UIHandler;
import net.firsp.amber.view.adapter.StatusListAdapter;
import net.firsp.amber.view.dialog.StatusDialogFragment;

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
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Status status = (Status) adapter.getItem(i);
                new StatusDialogFragment(StreamTimelineActivity.this, status).show(getFragmentManager(), "Status");
            }
        });
        account = Accounts.getInstance().getAccount(getIntent().getLongExtra("id",0));
        userStream = account.getTwitterStream();
        userStream.addListener(this);
        filterStream = account.getTwitterStream();
        filterStream.addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userStream.shutdown();
        filterStream.shutdown();
    }

    public void addStatus(final Status status) {
        if (!adapter.isCurrent()) {
            new UIHandler() {
                @Override
                public void run() {
                    addStatus(status);
                }
            };
            return;
        }
        int position = view.getFirstVisiblePosition();
        int yOffset = 0;
        if(view.getChildAt(0) != null){
            yOffset = view.getChildAt(0).getTop();
        }
        int added = adapter.addSorted(status);
        if (view.getFirstVisiblePosition() == 0 && view.getChildAt(0) != null && view.getChildAt(0).getTop() == 0){
            return;
        }
        if(position >= added){
            view.setSelectionFromTop(position + 1, yOffset);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.stream_action_tweet:
                DialogUtil.showTweetDialog(this, null);
                break;
            case R.id.stream_action_clear:
                adapter.clear(true);
                break;
        }
        return true;
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
