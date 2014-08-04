package net.firsp.amber.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NotifySettingAdapter extends BaseAdapter {

    Activity activity;

    List<String> data = new ArrayList<>();

    public NotifySettingAdapter(Activity a) {
        activity = a;
    }

    public void clear(){
        data.clear();
        notifyDataSetChanged();
    }

    public void add(String dat){
        data.add(dat);
        notifyDataSetChanged();
    }

    public void load() {
        FileInputStream fis = null;
        try {
            fis = activity.openFileInput("notify_filter");
            DataInputStream dis = new DataInputStream(fis);
            int cnt = dis.readInt();
            for (int i = 0; i < cnt; i++) {
                data.add(dis.readUTF());
            }
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        notifyDataSetChanged();
    }

    public void save() {
        FileOutputStream fos = null;
        try {
            fos = activity.openFileOutput("notify_filter", Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeInt(data.size());
            for (String s : data) {
                dos.writeUTF(s);
            }
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
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
        String data = getItem(i).toString();
        StringBuilder sb = new StringBuilder();
        switch (data.charAt(0)) {
            case 'u':
                sb.append("user:");
                break;
            case 't':
                sb.append("text:");
                break;
        }
        sb.append(data.substring(1));
        v.setText(sb.toString());
        return v;
    }
}
