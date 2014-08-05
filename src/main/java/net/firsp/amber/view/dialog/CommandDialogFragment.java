package net.firsp.amber.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.firsp.amber.util.Command;
import net.firsp.amber.util.FuncUtil;
import net.firsp.amber.view.model.EntityModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class CommandDialogFragment extends DialogFragment {

    Activity activity;
    LinkedList<Command> commands = new LinkedList<>();
    String title = "";

    public CommandDialogFragment(Activity activity, String title) {
        this.activity = activity;
        this.title = title;
    }

    public void addCommand(Command cmd){
        commands.add(cmd);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = new Dialog(activity);
        d.setTitle(title);

        ListView v = new ListView(activity);
        v.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, commands.toArray()));
        v.setOnItemClickListener((adapterView, view, i, l) -> {
            dismiss();
            Command cmd = (Command) adapterView.getItemAtPosition(i);
            cmd.execute();
        });

        d.setContentView(v);
        return d;
    }
}
