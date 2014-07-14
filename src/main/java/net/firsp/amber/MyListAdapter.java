package net.firsp.amber;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by owner on 2014/06/23.
 */
public class MyListAdapter extends BaseAdapter {

    Activity activity;

    public MyListAdapter(Activity a) {
        activity = a;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int i) {
        return new Object();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (i == 0) {
            TextView v = null;
            try {
                v = (TextView) activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
            } catch (Exception e) {
                v = new TextView(activity);
            }
            v.setText("TextView");
            return v;
        }
        Button v = new Button(activity);
        v.setText("Button");
        return v;
    }
}
