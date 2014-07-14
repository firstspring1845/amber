package net.firsp.amber.image;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncImageLoader extends AsyncTaskLoader<Bitmap> {

    String sn;
    String url;

    public AsyncImageLoader(Context context, String screenName, String url) {
        super(context);
        sn = screenName;
        this.url = url;
    }

    @Override
    public Bitmap loadInBackground() {
        try {
            Bitmap b = IconCache.getIcon(getContext(), sn, url);
            if (b != null) {
                return b;
            }
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.connect();

            InputStream is = con.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            byte[] buf = new byte[10240];
            int bufSize = 0;

            while ((bufSize = is.read(buf)) != -1) {
                os.write(buf, 0, bufSize);
            }

            os.close();
            byte[] data = os.toByteArray();

            IconCache.putIcon(getContext(), sn, url, data);
            return IconCache.getIcon(getContext(), sn, url);
        } catch (Exception e) {
            return null;
        }
    }
}
