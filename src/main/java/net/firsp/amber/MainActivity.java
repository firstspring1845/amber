package net.firsp.amber;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.firsp.amber.account.Account;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.util.AsyncTwitterUtil;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.DialogUtil;
import net.firsp.amber.util.Serializer;
import net.firsp.amber.util.ToastUtil;
import net.firsp.amber.util.UIHandler;
import net.firsp.amber.view.activity.NotifySettingActivity;
import net.firsp.amber.view.adapter.AccountListAdapter;
import net.firsp.amber.view.dialog.AccountDialogFragment;

import java.io.File;
import java.util.HashSet;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import twitter4j.AsyncTwitter;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class MainActivity extends ActionBarActivity {

    AccountListAdapter adapter;
    Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Accounts.initialize(this);
        ListView v = new ListView(this);
        adapter = new AccountListAdapter(this);
        adapter.setAccounts(Accounts.getInstance().getAccounts());
        v.setAdapter(adapter);
        v.setOnItemClickListener((adapterView, view, i, l) -> {
            account = (Account) adapterView.getItemAtPosition(i);
            new AccountDialogFragment(this, account).show(getFragmentManager(), "account");
        });
        setContentView(v);
    }

    public void refresh(){
        adapter.notifyDataSetInvalidated();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UIHandler handler = new UIHandler();
        int id = item.getItemId();
        if (id == R.id.action_authorize) {
            TextView text = new TextView(this);
            text.setText("APIキーを入れてね、空の場合デフォルトになります。");
            final EditText consumer = new EditText(this);
            final EditText secret = new EditText(this);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.addView(text);
            layout.addView(consumer);
            layout.addView(secret);

            new AlertDialog.Builder(this)
                    .setTitle("APIキー設定")
                    .setView(layout)
                    .setPositiveButton("発射", (di, i) -> {
                        ProgressDialog d = DialogUtil.createProgress(this);
                        d.show();

                        new Thread(() -> {
                            try {
                                Twitter t = new TwitterFactory().getInstance();
                                StringBuilder consumerKey = new StringBuilder();
                                StringBuilder consumerSecret = new StringBuilder();
                                if (!"".equals(consumer.getText().toString()) && !"".equals(secret.getText().toString())) {
                                    consumerKey.append(consumer.getText());
                                    consumerSecret.append(secret.getText());

                                } else {
                                    consumerKey.append("lNO8K0sLqeVagRam1Vr52A");
                                    consumerSecret.append("uW3vxLkLt6uKBGkN2kJDqGv5c8pItYZTi16G0Q3xnik");
                                }
                                t.setOAuthConsumer(consumerKey.toString(), consumerSecret.toString());
                                RequestToken rt = t.getOAuthRequestToken();
                                d.dismiss();
                                handler.post(() -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(rt.getAuthorizationURL()));
                                    startActivity(intent);
                                    EditText editText = new EditText(this);
                                    new AlertDialog.Builder(this)
                                            .setTitle("PINコードを入力して")
                                            .setView(editText)
                                            .setPositiveButton("発射", (di_, i_) -> {
                                                d.show();
                                                new Thread(() -> {
                                                    try {
                                                        AccessToken token = t.getOAuthAccessToken(rt, editText.getText().toString());
                                                        Account a = new Account(consumerKey.toString(),
                                                                consumerSecret.toString(),
                                                                token.getToken(),
                                                                token.getTokenSecret(),
                                                                token.getUserId(),
                                                                token.getScreenName());
                                                        Accounts.getInstance().putAccount(a);
                                                        Accounts.getInstance().setDefaultAccount(a);
                                                        adapter.setAccounts(Accounts.getInstance().getAccounts());
                                                    } catch (Exception e) {
                                                        DialogUtil.showException(this, e);
                                                    }
                                                    d.dismiss();
                                                }).start();
                                            })
                                            .create()
                                            .show();
                                });
                            } catch (Exception e) {
                                DialogUtil.showException(this, e);
                            }
                        }).start();
                    })
                    .create()
                    .show();


            return true;
        }
        if (id == R.id.action_backup) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "firsp");
            File file = new File(dir, "accounts.dat");
            dir.mkdirs();
            Serializer.write(file, Accounts.getInstance().getAccounts());
        }
        if (id == R.id.action_restore) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "firsp");
            File file = new File(dir, "accounts.dat");
            Object data = Serializer.read(file);
            if (data instanceof Account[]) {
                Account[] accounts = (Account[]) data;
                for (Account account1 : accounts) {
                    Accounts.getInstance().putAccount(account1);
                }
            }
            if (Accounts.getInstance().getDefaultAccount() == null) {
                Account[] accounts = Accounts.getInstance().getAccounts();
                if (accounts.length != 0) {
                    Accounts.getInstance().setDefaultAccount(accounts[0]);
                }
            }
            adapter.setAccounts(Accounts.getInstance().getAccounts());
        }
        if (id == R.id.action_notify_setting) {
            startActivity(new Intent(this, NotifySettingActivity.class));
        }
        if (id == R.id.action_cache) {
            ProgressDialog d = DialogUtil.createProgress(this);
            d.show();
            new Thread(() -> {
                try {
                    long[] ids = Accounts.getInstance().getDefaultAccount().getTwitter().getFriendsIDs(-1).getIDs();
                    HashSet<String> set = new HashSet<String>();
                    for (int i = 0; i < ids.length; i++) {
                        set.add(String.valueOf(ids[i]));
                    }
                    File cache = new File(getCacheDir().getAbsoluteFile(), "cache");
                    long deletebyte = 0;
                    for (File dir : cache.listFiles()) {
                        if (!set.contains(dir.getName())) {
                            deletebyte += delete(dir);
                        }
                    }
                    CroutonUtil.showText(this, "" + deletebyte + "バイトのファイルが削除されました");
                } catch (Exception e) {
                    CroutonUtil.error(this);
                }
                d.dismiss();
            }).start();
        }
        return super.onOptionsItemSelected(item);
    }

    long delete(File dir) {
        long deletebyte = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                deletebyte += file.length();
                file.delete();
            }
            if (file.isDirectory()) {
                deletebyte += delete(file);
            }
        }
        dir.delete();
        return deletebyte;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyLongPress(keyCode, event);
    }

    // Call from AccountDialogFragment#updateIcon
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            AsyncTwitter t = account.getAsyncTwitter();
            t.addListener(AsyncTwitterUtil.getTwitterListener(this));
            t.updateProfileImage(getContentResolver().openInputStream(data.getData()));
        } catch (Exception e) {
            ToastUtil.error(this);
        }
    }

}
