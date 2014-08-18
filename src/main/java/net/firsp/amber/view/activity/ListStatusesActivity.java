package net.firsp.amber.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.firsp.amber.R;
import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.filter.NopFilter;
import net.firsp.amber.filter.StatusFilter;
import net.firsp.amber.filter.UserFilter;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.view.adapter.StatusListAdapter;
import net.firsp.amber.view.dialog.StatusDialogFragment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
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

public class ListStatusesActivity extends StreamTimelineActivity {

    StatusFilter filter = NopFilter.INSTANCE;
    long[] follows;

    long listId;
    Thread rest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Accounts.initialize(this);

        Intent intent = getIntent();

        listId = intent.getLongExtra("id", 0);
        setTitle(intent.getStringExtra("title"));
        account = Accounts.getInstance().getAccount(intent.getLongExtra("account", 0));
        File file = new File(new File(getCacheDir().getAbsolutePath(), "lists"), String.valueOf(listId));

        ProgressDialog d = DialogUtil.createProgress(this);
        d.show();
        new Thread(() -> {
            try {
                DataInputStream data = new DataInputStream(new FileInputStream(file));
                follows = new long[data.readInt()];
                for (int i = 0; i < follows.length; i++) {
                    follows[i] = data.readLong();
                }
                data.close();
            } catch (Exception e) {
                getListMember();
            }
            userStream = account.getTwitterStream();
            userStream.addListener(ListStatusesActivity.this);
            filterStream = account.getTwitterStream();
            filterStream.addListener(ListStatusesActivity.this);
            filter = new UserFilter(follows);
            userStream.user();
            filterStream.filter(new FilterQuery(follows));
            try {
                Twitter twitter = account.getTwitter();
                List<Status> list = twitter.getUserListStatuses(listId, new Paging(1, 200));
                for (Status status : list) {
                    adapter.add(status);
                }
                adapter.refresh();
            } catch (Exception e) {
                CroutonUtil.error(this);
            }
            restRefresh();
            d.dismiss();
        }).start();
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
            ProgressDialog d = DialogUtil.createProgress(this);
            d.show();
            new Thread(() -> {
                getListMember();
                d.dismiss();
            }).start();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            rest.stop();
        } catch (Exception e) {
        }
    }

    //StatusListener Implements

    @Override
    public void onStatus(Status status) {
        if (filter.filter(status)) {
            addStatus(status);
        }
    }

    //User

    public void restRefresh() {
        rest = new Thread(() -> {
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
        });
        rest.start();
    }

    public void getListMember() {
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
            DataOutputStream data = new DataOutputStream(new FileOutputStream(file));
            data.writeInt(members.size());
            follows = new long[members.size()];
            for (int i = 0; i < members.size(); i++) {
                User user = members.get(i);
                data.writeLong(user.getId());
                follows[i] = user.getId();
            }
            data.close();
        } catch (Exception e) {
            CroutonUtil.error(this);
        }
    }
}
