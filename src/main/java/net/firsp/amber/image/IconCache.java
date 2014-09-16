package net.firsp.amber.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import net.firsp.amber.util.Callback;
import net.firsp.amber.util.HttpDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.concurrent.LinkedBlockingQueue;

public class IconCache implements Runnable {

    public IconCache(Context context) {
        this.context = context;
        new Thread(this).start();
    }

    Context context;
    Bitmap def;
    LruCache<String, Bitmap> memCache = new LruCache<String, Bitmap>(5242880); //5MB
    LinkedBlockingQueue<File> paths = new LinkedBlockingQueue<File>();
    LinkedBlockingQueue<String> urls = new LinkedBlockingQueue<String>();
    LinkedBlockingQueue<Callback> callbacks = new LinkedBlockingQueue<Callback>();

    public Bitmap getIcon(long id, String url, Callback callback) {
        Bitmap b = memCache.get(url);
        if (b != null) {
            return b;
        }
        File path = getFilePath(id, url);
        try {
            FileInputStream fis = new FileInputStream(path);
            byte[] data = new byte[(int) path.length()];
            fis.read(data);
            b = putIcon(path, url, data, false);
            if (b != null) {
                return b;
            }
        } catch (Exception e) {
        }
        paths.add(path);
        urls.add(url);
        callbacks.add(callback);
        if (def == null) {
            int imgSize = (int) (context.getResources().getDisplayMetrics().densityDpi / 160F * 48);
            def = Bitmap.createBitmap(imgSize, imgSize, Bitmap.Config.ARGB_8888);
        }
        return def;
    }

    public Bitmap putIcon(File path, String url, byte[] data, boolean putLocal) {
        //ImageViewが激最悪なのでここでリサイズ
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
        int imgSize = (int) (context.getResources().getDisplayMetrics().densityDpi / 160F * 48);
        b = Bitmap.createScaledBitmap(b, imgSize, imgSize, true);
        memCache.put(url, b);
        FileOutputStream fos = null;
        if (putLocal) {
            try {
                path.getParentFile().mkdirs();
                fos = new FileOutputStream(path);
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                try {
                    fos.close();
                } catch (Exception e1) {
                }
            }
        }
        return b;
    }

    public File getFilePath(long id, String url) {
        File cache = new File(context.getCacheDir().getAbsoluteFile(), "cache");
        File dir = new File(cache, String.valueOf(id));
        return new File(dir, getMD5String(url));
    }

    public static String getMD5String(String src) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(src.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : md5) {
                sb.append(Character.forDigit(b >> 4 & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return ":(";
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                File path = paths.take();
                String url = urls.take();
                Callback callback = callbacks.take();
                Bitmap b = memCache.get(url);
                if (b == null) {
                    byte[] data = HttpDownloader.download(url);
                    if (data != null) {
                        b = putIcon(path, url, data, true);
                    }
                }
                if (b != null) {
                    callback.callback(b);
                }
            } catch (Exception e) {
            }
        }
    }

}
