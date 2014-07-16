package net.firsp.amber.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.firsp.amber.view.model.EntityModel;

import java.util.ArrayList;
import java.util.Arrays;

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

        ArrayList<EntityModel> l = new ArrayList<EntityModel>();
        for (UserMentionEntity userMentionEntity : status.getUserMentionEntities()) {
            l.add(new EntityModel(userMentionEntity));
        }
        for (URLEntity urlEntity : status.getURLEntities()) {
            l.add(new EntityModel(urlEntity));
        }
        for (MediaEntity mediaEntity : status.getMediaEntities()) {
            l.add(new EntityModel(mediaEntity));
        }

        ListView v = new ListView(activity);
        v.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, l.toArray()));
        v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dismiss();
                EntityModel m = (EntityModel) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m.getContentUrl()));
                activity.startActivity(intent);
            }
        });

        d.setContentView(v);
        return d;
    }
}
