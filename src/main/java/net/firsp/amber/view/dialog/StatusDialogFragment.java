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
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.view.activity.UserTimelineActivity;

import java.util.ArrayList;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.Twitter;

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
        Twitter t = Accounts.getInstance().getDefaultAccount().getTwitter();
        Status original = status.isRetweet() ? status.getRetweetedStatus() : status;
        switch (i) {
            case 0:
                DialogUtil.showTweetDialog(activity, original);
                break;
            case 1:
                new Thread(()->{
                   try{
                       t.createFavorite(original.getId());
                       CroutonUtil.showText(activity, "ふぁぼふぁぼしたんたん");
                   }catch(Exception e){
                       CroutonUtil.error(activity);
                   }
                }).start();
                break;
            case 2:
                new Thread(()->{
                   try{
                       t.destroyFavorite(original.getId());
                       CroutonUtil.showText(activity, "あんふぁぼしたんたん");
                   }catch(Exception e){
                       CroutonUtil.error(activity);
                   }
                }).start();
                break;
            case 3:
                new AlertDialog.Builder(activity)
                        .setMessage("リツイートしてもいい？")
                        .setPositiveButton("よろしい", (di,ii)->{
                            new Thread(()->{
                               try{
                                   t.retweetStatus(original.getId());
                                   CroutonUtil.showText(activity, "リツイートしたんたん");
                               }catch(Exception e){
                                   CroutonUtil.error(activity);
                               }
                            }).start();
                        })
                        .setNegativeButton("だめ",(di,ii)->{})
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
