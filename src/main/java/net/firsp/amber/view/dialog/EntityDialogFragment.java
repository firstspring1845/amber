package net.firsp.amber.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.firsp.amber.util.FuncUtil;
import net.firsp.amber.view.model.EntityModel;

import java.util.ArrayList;
import java.util.Collections;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class EntityDialogFragment extends DialogFragment {

    Activity activity;
    Status status;

    public EntityDialogFragment(Activity activity, Status status) {
        this.activity = activity;
        this.status = status;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(activity);
        d.setTitle("情報");

        ArrayList<Object> list = new ArrayList<>();
        list.add(status.getUser());
        Collections.addAll(list, status.getUserMentionEntities());
        Collections.addAll(list, status.getURLEntities());
        Collections.addAll(list, status.getMediaEntities());

        ListView v = new ListView(activity);
        v.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, FuncUtil.map(list, (a)->new EntityModel(a)).toArray()));
        v.setOnItemClickListener((adapterView, view, i, l) -> {
            dismiss();
            EntityModel m = (EntityModel) adapterView.getItemAtPosition(i);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m.getContentUrl()));
            activity.startActivity(intent);
        });

        d.setContentView(v);
        return d;
    }
}
