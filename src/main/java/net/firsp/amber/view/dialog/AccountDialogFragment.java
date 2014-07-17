package net.firsp.amber.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.AsyncTwitterUtil;
import net.firsp.amber.view.activity.UserStreamActivity;

import java.util.Arrays;

import twitter4j.AsyncTwitter;

public class AccountDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    Activity activity;
    Account account;

    public AccountDialogFragment(Activity activity, Account account) {
        this.activity = activity;
        this.account = account;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(activity);
        d.setTitle(account.getScreenName());
        ListView v = new ListView(activity);
        v.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, Arrays.asList("名前変更", "アイコン変更", "リスト管理", "ストリーミング", "デフォルトに設定").toArray()));
        v.setOnItemClickListener(this);
        d.setContentView(v);
        return d;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        dismiss();
        switch (i) {
            case 0:
                updateName();
                break;
            case 1:
                updateIcon();
                break;
            case 2:
                new TwitterListDialogFragment(activity, account).show(getFragmentManager(), "list");
                break;
            case 3:
                Intent intent = new Intent(activity, UserStreamActivity.class);
                intent.putExtra("id", account.getId());
                activity.startActivity(intent);
                break;
            case 4:
                Accounts.getInstance().setDefaultAccount(account);
                break;
        }
    }

    void updateName() {
        final EditText name = new EditText(activity);
        new AlertDialog.Builder(activity)
                .setTitle("名前を入れてね")
                .setView(name)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AsyncTwitter t = account.getAsyncTwitter();
                        t.addListener(AsyncTwitterUtil.getTwitterListener(activity));
                        t.updateProfile(name.getText().toString(), null, null, null);
                    }
                })
                .create()
                .show();
    }

    void updateIcon() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        activity.startActivityForResult(intent, 0);
    }
}
