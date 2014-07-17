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
import android.widget.EditText;
import android.widget.ListView;

import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.AsyncTwitterUtil;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.view.activity.UserTimelineActivity;

import java.util.ArrayList;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.auth.AccessToken;

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
        l.add("リプを送る");
        l.add("ふぁぼる");
        l.add("あんふぁぼする");
        l.add("リツイートする");
        l.add("ツイートURLを開く");
        l.add("ユーザーページを開く");
        l.add("ユーザーTLを開く");
        l.add("ツイート内URL情報");
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
                final EditText editText = new EditText(activity);
                editText.setText("@" + original.getUser().getScreenName() + " ");
                new AlertDialog.Builder(activity)
                        .setTitle("Reply")
                        .setView(editText)
                        .setPositiveButton("発射", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                t.updateStatus(new StatusUpdate(editText.getText().toString()).inReplyToStatusId(original.getId()));
                            }
                        })
                        .create()
                        .show();
                break;
            case 1:
                t.createFavorite(original.getId());
                break;
            case 2:
                t.destroyFavorite(original.getId());
                break;
            case 3:
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
            case 4:
                StringBuilder sb = new StringBuilder();
                sb.append("https://twitter.com/");
                sb.append(original.getUser().getScreenName());
                sb.append("/status/");
                sb.append(original.getId());

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString()));
                activity.startActivity(intent);
                break;
            case 5:
                sb = new StringBuilder();
                sb.append("https://twitter.com/");
                sb.append(original.getUser().getScreenName());

                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sb.toString()));
                activity.startActivity(intent);
                break;
            case 6:
                intent = new Intent(activity, UserTimelineActivity.class);
                intent.putExtra("screen_name", original.getUser().getScreenName());
                activity.startActivity(intent);
                break;
            case 7:
                new EntityDialogFragment(activity, status).show(activity.getFragmentManager(), "Entity");
                break;
        }
    }
}
