package net.firsp.amber.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.firsp.amber.account.Account;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.util.Serializer;
import net.firsp.amber.util.ToastUtil;
import net.firsp.amber.view.activity.ListStatusesActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.UserList;

public class TwitterListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    Activity activity;
    Account account;
    UserList[] lists = new UserList[0];

    public TwitterListDialogFragment(Activity activity, Account account) {
        this.activity = activity;
        this.account = account;
        Object data = Serializer.read(new File(account.getAccountDir(activity), "lists.dat"));
        if (data instanceof UserList[]) {
            lists = (UserList[]) data;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(activity);
        d.setTitle(account.getScreenName());
        ListView v = new ListView(activity);
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < lists.length; i++) {
            list.add(lists[i].getFullName());
        }
        list.add("更新");
        v.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, list.toArray()));
        v.setOnItemClickListener(this);
        d.setContentView(v);
        return d;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        dismiss();
        if (i == lists.length) {
            ProgressDialog d = DialogUtil.createProgress(activity);
            d.show();
            new Thread(() -> {
                try {
                    UserList[] arr = account.getTwitter().getUserLists(account.getId()).toArray(new UserList[0]);
                    File accountDir = account.getAccountDir(activity);
                    accountDir.mkdirs();
                    Serializer.write(new File(accountDir, "lists.dat"),
                            account.getTwitter().getUserLists(account.getId()).toArray(new UserList[0]));
                } catch (Exception e) {
                    ToastUtil.error(activity);
                }
                d.dismiss();
            }).start();
        } else {
            Intent intent = new Intent(activity, ListStatusesActivity.class);
            intent.putExtra("id", lists[i].getId());
            intent.putExtra("title", lists[i].getFullName());
            intent.putExtra("account", account.getId());

            activity.startActivity(intent);
        }
    }

}
