package net.firsp.amber.view.adapter;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.firsp.amber.account.Account;
import net.firsp.amber.util.UIHandler;

public class AccountListAdapter extends BaseAdapter {

    Activity activity;

    Account[] data = new Account[0];

    public AccountListAdapter(Activity a) {
        activity = a;
    }

    public void setAccounts(Account[] accounts) {
        data = accounts;
        new UIHandler().post(() -> {
            notifyDataSetChanged();
        });
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return data[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView v = null;
        try {
            v = (TextView) activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        } catch (Exception e) {
            v = new TextView(activity);
        }
        v.setText(((Account) getItem(i)).getScreenName());
        return v;
    }
}
