package net.firsp.amber.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.AsyncTwitterUtil;

import java.util.ArrayList;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.Status;

public class StatusDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    Activity activity;
    Status status;

    public StatusDialogFragment(Activity activity, Status status) {
        this.activity = activity;
        this.status = status;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(activity);
        d.setTitle(status.getText());
        ListView v = new ListView(activity);
        List<String> l = new ArrayList<String>();
        l.add("ふぁぼる");
        l.add("あんふぁぼする");
        l.add("リツイートする");
        l.add("ツイートURLを開く");
        l.add("ユーザーページを開く");
        v.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, l.toArray()));
        v.setOnItemClickListener(this);
        d.setContentView(v);
        return d;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        dismiss();
        final AsyncTwitter t = Accounts.getInstance().getDefaultAccount().getAsyncTwitter();
        t.addListener(AsyncTwitterUtil.getTwitterListener(activity));
        final Status original = status.isRetweet() ? status.getRetweetedStatus() : status;
        switch (i) {
            case 0:
                t.createFavorite(original.getId());
                break;
            case 1:
                t.destroyFavorite(original.getId());
                break;
            case 2:
                new AlertDialog.Builder(activity)
                        .setMessage("リツイートしてもいい？")
                        .setPositiveButton("よろしい", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                t.retweetStatus(original.getId());
                            }
                        })
                        .setNegativeButton("だめ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();
                break;
            case 3:
                StringBuilder sb = new StringBuilder();
                sb.append("https://twitter.com/");
                sb.append(original.getUser().getScreenName());
                sb.append("/status/");
                sb.append(original.getId());

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString()));
                activity.startActivity(intent);
                break;
            case 4:
                sb = new StringBuilder();
                sb.append("https://twitter.com/");
                sb.append(original.getUser().getScreenName());

                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString()));
                activity.startActivity(intent);
                break;
        }
    }
}
