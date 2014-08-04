package net.firsp.amber.view.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import net.firsp.amber.R;
import net.firsp.amber.account.Accounts;
import net.firsp.amber.view.adapter.NotifySettingAdapter;

public class NotifySettingActivity extends ActionBarActivity {

    NotifySettingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView v = new ListView(this);
        adapter = new NotifySettingAdapter(this);
        adapter.load();
        v.setAdapter(adapter);
        v.setOnItemClickListener((adapterView, view, i, l) -> {
        });
        setContentView(v);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.notify_add:
                EditText editText = new EditText(this);
                new AlertDialog.Builder(this)
                        .setTitle("通知したいユーザーのSN頼む")
                        .setView(editText)
                        .setPositiveButton("発射", (di, i) -> {
                            adapter.add("u" + editText.getText());
                            adapter.save();
                        })
                        .create()
                        .show();
                break;
            case R.id.notify_remove:
                adapter.clear();
                adapter.save();
        }
        return true;
    }

}
