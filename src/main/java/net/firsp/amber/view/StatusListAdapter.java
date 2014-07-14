package net.firsp.amber.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.firsp.amber.R;
import net.firsp.amber.image.IconCache;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.HttpDownloader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import twitter4j.Status;

public class StatusListAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    Activity activity;

    public boolean requireRefresh;

    public StatusListAdapter(Activity activity) {
        this.activity = activity;
    }

    Map<Long, Status> statuses = new ConcurrentHashMap<Long, Status>();
    List<Status> statusList = Collections.synchronizedList(new ArrayList<Status>());

    public void add(Status status) {
        statuses.put(status.getId(), status);
    }

    private boolean isCurrent() {
        return Thread.currentThread().equals(Looper.getMainLooper().getThread());
    }

    public void refresh() {
        List<Status> list = new ArrayList<Status>(statuses.values());
        Collections.sort(list, new Comparator<Status>() {
            @Override
            public int compare(Status status, Status status2) {
                return Long.valueOf(status.getId()).compareTo(status2.getId());
            }
        });
        Collections.reverse(list);
        statusList = Collections.synchronizedList(list);
        if (isCurrent()) {
            notifyDataSetChanged();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public int getCount() {
        return statusList.size();
    }

    @Override
    public Object getItem(int i) {
        return statusList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //@Override
    public View getView_(int i, View view, ViewGroup viewGroup) {
        ViewGroup group = new LinearLayout(activity);
        ImageView img = new ImageView(activity);
        img.setImageResource(R.drawable.unh7);
        TextView v = null;
        try {
            v = (TextView) activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        } catch (Exception e) {
            v = new TextView(activity);
        }
        v.setText("チンポ\nソイヤ\nアナル\nアヌス");
        group.addView(img);
        group.addView(v);
        return group;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Status status = statusList.get(i);
        Status original = status.isRetweet() ? status.getRetweetedStatus() : status;

        final String sn = original.getUser().getScreenName();
        final String url = original.getUser().getOriginalProfileImageURL();

        ViewGroup group = new LinearLayout(activity);

        final ImageView img = new ImageView(activity);

        final int imgSize = (int) (activity.getResources().getDisplayMetrics().densityDpi / 160F * 48);
        img.setMinimumWidth(imgSize);
        img.setMinimumHeight(imgSize);
        img.setMaxWidth(imgSize);
        img.setMaxWidth(imgSize);
        //img.setScaleType(ImageView.ScaleType.FIT_XY);
        //これ入れないとsetMax～が無視されるらしい fuck
        //setAdjustViewBoundsすると画像が拡大されなくなるんですよ
        //img.setAdjustViewBounds(true);
        //img.setImageResource(R.drawable.unh7);
        Bitmap b = IconCache.getIcon(activity, sn, url);
        if (b != null) {
            img.setImageBitmap(b);
        } else {
            b = BitmapFactory.decodeResource(activity.getResources(), R.drawable.unh7);
            b = Bitmap.createScaledBitmap(b, imgSize, imgSize, true);
            img.setImageBitmap(b);
            new Thread() {
                @Override
                public void run() {
                    Bitmap b = IconCache.getIcon(activity, sn, url);
                    if (b == null) {
                        byte[] data = HttpDownloader.download(url);
                        if (data != null) {
                            IconCache.putIcon(activity, sn, url, data);
                            b = IconCache.getIcon(activity, sn, url);
                        }
                    }
                    if (b != null) {
                        final Bitmap bit = b;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageBitmap(bit);
                            }
                        });
                    }
                }
            }.start();
        }

        TextView v = null;
        try {
            v = (TextView) activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
        } catch (Exception e) {
            v = new TextView(activity);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(original.getUser().getScreenName());
        sb.append(": ");
        sb.append(original.getText());

        sb.append("\n");

        Calendar cal = Calendar.getInstance();
        cal.setTime(status.getCreatedAt());

        sb.append(String.format("%04d/%02d/%02d %02d:%02d:%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DATE),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)));

        if (status != original) {
            sb.append(" RT:");
            sb.append(status.getUser().getScreenName());
        }

        v.setText(sb.toString());

        group.addView(img);
        group.addView(v);

        return group;
    }

    //OnScrollListener Implements

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if (!requireRefresh) {
            return;
        }
        if (absListView.getFirstVisiblePosition() == 0 && absListView.getChildAt(0) != null && absListView.getChildAt(0).getTop() == 0) {
            requireRefresh = false;
            int before = getCount();
            refresh();
            int after = getCount();
            int adds = after - before;
            absListView.setSelection(adds);
            if (adds != 0) {
                CroutonUtil.showText(activity, "" + adds + "件追加しました");
                //ToastUtil.show(this, "" + adds + "件追加しました");
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {

    }
}
