package net.firsp.amber.common;

import android.content.Context;

import net.firsp.amber.filter.NopFilter;
import net.firsp.amber.filter.OrFilter;
import net.firsp.amber.filter.StatusFilter;
import net.firsp.amber.filter.TextFilter;
import net.firsp.amber.filter.UserNameFilter;
import net.firsp.amber.util.ToastUtil;

import java.io.DataInputStream;
import java.io.FileInputStream;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class Notificator implements StatusListener {

    Context context;

    StatusFilter filter;

    private static final Notificator INSTANCE = new Notificator();

    private Notificator() {
    }

    public static Notificator getInstance() {
        return INSTANCE;
    }

    public static void dispose() {
        INSTANCE.context = null;
        INSTANCE.filter = NopFilter.INSTANCE;
    }

    public static Notificator initialize(Context context) {
        Notificator n = getInstance();
        n.context = context;
        n.filter = NopFilter.INSTANCE;
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("notify_filter");
            DataInputStream dis = new DataInputStream(fis);
            int cnt = dis.readInt();
            for (int i = 0; i < cnt; i++) {
                String data = dis.readUTF();
                StatusFilter filter = NopFilter.INSTANCE;
                switch (data.charAt(0)) {
                    case 'u':
                        filter = UserNameFilter.of(data.substring(1));
                        break;
                    case 't':
                        filter = TextFilter.of(data.substring(1));
                        break;
                }
                n.filter = OrFilter.of(n.filter, filter);
            }
        } catch (Exception e) {
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return INSTANCE;
    }

    @Override
    public void onStatus(Status status) {
        if (filter.filter(status)) {
            ToastUtil.show(context, status.getUser().getScreenName() + ":" + status.getText());
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    @Override
    public void onTrackLimitationNotice(int i) {

    }

    @Override
    public void onScrubGeo(long l, long l2) {

    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {

    }

    @Override
    public void onException(Exception e) {

    }
}
