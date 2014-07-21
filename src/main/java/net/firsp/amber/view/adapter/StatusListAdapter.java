package net.firsp.amber.view.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.firsp.amber.image.IconCache;
import net.firsp.amber.util.Callback;
import net.firsp.amber.util.CroutonUtil;
import net.firsp.amber.util.UIHandler;

import java.text.SimpleDateFormat;
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

    //OnScrollLIstenerによるツイート追加をする場合はrequireRefreshをtrueにすること
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
            new UIHandler(){
                @Override
                public void run(){
                    notifyDataSetChanged();
                }
            };

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

        sb.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(status.getCreatedAt()));

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
    public void onScrollStateChanged(AbsListView absListView, int state) {
        if(true){
            //流星っぽいの
            //スクロール中は何もしない
            if(state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                return;
            }
            try{
                int pos = absListView.getFirstVisiblePosition();
                int off = absListView.getChildAt(0).getTop();
                long id = statusList.get(pos).getId();
                refresh();
                for (int j = 0; j < statusList.size(); j++) {
                    Status status = statusList.get(j);
                    if(status.getId() == id){
                        pos = j;
                        break;
                    }
                }
                ((ListView)absListView).setSelectionFromTop(pos, off);
            }catch(Exception e){
                //怠慢プログラミング最高ｗ
            }
            return;
        }
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
