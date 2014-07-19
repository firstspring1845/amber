package net.firsp.amber.view.adapter;

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
import net.firsp.amber.util.Callback;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.HttpDownloader;
import net.firsp.amber.util.ToastUtil;
import net.firsp.amber.util.UIHandler;

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
    IconCache cache;

    public boolean requireRefresh;

    public StatusListAdapter(Activity activity) {
        this.activity = activity;
        cache = new IconCache(activity);
    }

    Map<Long, Status> statuses = new ConcurrentHashMap<Long, Status>();
    List<Status> statusList = Collections.synchronizedList(new ArrayList<Status>());

    //返り値は挿入位置 挿入しない場合-1
    //bisectは神
    //UIスレッドから呼び出し
    public int addSorted(Status status){
        if(!statuses.containsKey(status.getId())){
            synchronized(this){
                int lo = 0;
                int hi = statusList.size();
                while(lo < hi){
                    int mid = (lo + hi) >> 2;
                    //降順なので逆
                    if(statusList.get(mid).getId() > status.getId()){
                        lo = mid + 1;
                    }else{
                        hi = mid;
                    }
                }
                statusList.add(lo, status);
                statuses.put(status.getId(), status);
                notifyDataSetChanged();
                return lo;
            }
        }
        return -1;
    }

    public void add(Status status) {
        statuses.put(status.getId(), status);
    }

    public static boolean isCurrent() {
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

    public void clear(boolean hold){
        statuses.clear();
        if(hold){
            for (Status status : statusList.subList(0,3)) {
                statuses.put(status.getId(), status);
            }
        }
        refresh();
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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Status status = statusList.get(i);
        Status original = status.isRetweet() ? status.getRetweetedStatus() : status;

        final long id = original.getUser().getId();
        final String url = original.getUser().getOriginalProfileImageURL();

        final ViewGroup group = new LinearLayout(activity);

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
        img.setImageBitmap(cache.getIcon(id, url, new Callback() {
            @Override
            public void callback(final Object callback) {
                new UIHandler(){
                    @Override
                    public void run() {
                        img.setImageBitmap((Bitmap)callback);
                    }
                };
            }
        }));

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

        sb.append(" via ");
        sb.append(original.getSource().replaceAll("<.*?>",""));

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
