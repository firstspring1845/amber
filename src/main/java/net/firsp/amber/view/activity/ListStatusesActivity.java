package net.firsp.amber.view.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import net.firsp.amber.R;
import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.filter.NopFilter;
import net.firsp.amber.filter.StatusFilter;
import net.firsp.amber.filter.UserFilter;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.util.Serializer;
import net.firsp.amber.util.ToastUtil;
import net.firsp.amber.view.StatusListAdapter;
import net.firsp.amber.view.dialog.StatusDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import twitter4j.FilterQuery;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.User;

public class ListStatusesActivity extends ActionBarActivity implements StatusListener {

    StatusListAdapter adapter;

    //Queue<Status> streamStatus = new ConcurrentLinkedQueue<Status>();
    boolean requireRefresh = false;
    StatusFilter filter = NopFilter.INSTANCE;


    long listId;
    Account account;
    TwitterStream userStream;
    TwitterStream filterStream;
    Thread rest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Accounts.initialize(this);
        ListView v = new ListView(this);
        adapter = new StatusListAdapter(this);
        v.setAdapter(adapter);
        v.setOnScrollListener(adapter);
        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Status status = (Status) adapter.getItem(i);
                new StatusDialogFragment(ListStatusesActivity.this, status).show(getFragmentManager(), "Status");
            }
        });
        setContentView(v);

        Intent intent = getIntent();

        listId = intent.getLongExtra("id", 0);
        setTitle(intent.getStringExtra("title"));
        account = Accounts.getInstance().getAccount(intent.getLongExtra("account", 0));

        new Thread() {
            @Override
            public void run() {
                try {
                    Twitter twitter = account.getTwitter();
                    List<Status> list = twitter.getUserListStatuses(listId, new Paging(1, 200));
                    for (Status status : list) {
                        adapter.add(status);
                    }
                    adapter.refresh();
                } catch (Exception e) {
                    DialogUtil.showException(ListStatusesActivity.this, e);
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.list_action_get_member) {
            final ProgressDialog d = DialogUtil.createProgress(this);
            d.show();
            new Thread() {
                @Override
                public void run() {
                    try {
                        List<User> members = new ArrayList<User>();
                        Twitter t = account.getTwitter();
                        long cursor = -1;
                        while (true) {
                            PagableResponseList<User> l = t.getUserListMembers(listId, cursor);
                            cursor = l.getNextCursor();
                            members.addAll(l);
                            if (l.isEmpty()) {
                                break;
                            }
                        }
                        File dir = new File(getCacheDir().getAbsolutePath(), "lists");
                        dir.mkdirs();
                        File file = new File(dir, String.valueOf(listId));
                        Serializer.write(file, members.toArray(new User[members.size()]));
                    } catch (Exception e) {
                        ToastUtil.error(ListStatusesActivity.this);
                    }
                    d.dismiss();
                }
            }.start();
        }
        if (id == R.id.list_action_show_member) {
            File file = new File(new File(getCacheDir().getAbsolutePath(), "lists"), String.valueOf(listId));
            Object data = Serializer.read(file);
            if (data instanceof User[]) {
                User[] users = (User[]) data;
                StringBuilder sb = new StringBuilder();
                for (User user : users) {
                    sb.append(user.getScreenName());
                    sb.append("\n");
                }
                new AlertDialog.Builder(this)
                        .setMessage(sb.toString())
                        .create()
                        .show();
            }
        }
        if (id == R.id.list_action_streaming) {
            File file = new File(new File(getCacheDir().getAbsolutePath(), "lists"), String.valueOf(listId));
            Object data = Serializer.read(file);
            if (data instanceof User[]) {
                User[] users = (User[]) data;
                List<Long> ids = new LinkedList<Long>();
                long[] idsArray = new long[users.length];
                for (int i = 0; i < users.length; i++) {
                    User user = users[i];
                    ids.add(user.getId());
                    idsArray[i] = user.getId();
                }
                filter = new UserFilter(ids);
                if (filterStream == null) {
                    filterStream = account.getTwitterStream();
                    filterStream.addListener(this);
                    filterStream.filter(new FilterQuery(idsArray));
                }
            }
            if (userStream == null) {
                userStream = account.getTwitterStream();
                userStream.addListener(this);
                userStream.user();
            }
            restRefresh();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            userStream.shutdown();
        } catch (Exception e) {
        }
        try {
            filterStream.shutdown();
        } catch (Exception e) {
        }
        try {
            rest.stop();
        } catch (Exception e) {
        }
    }

    //StatusListener Implements

    @Override
    public void onStatus(Status status) {
        if (filter.filter(status)) {
            adapter.add(status);
            adapter.requireRefresh = true;
        }
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

    //User

    public void restRefresh() {
        rest = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                    Twitter twitter = account.getTwitter();
                    List<Status> list = twitter.getUserListStatuses(listId, new Paging(1, 200));
                    for (Status status : list) {
                        adapter.add(status);
                    }
                    adapter.requireRefresh = true;
                } catch (Exception e) {
                }
                restRefresh();
            }
        };
        rest.start();
    }
}
