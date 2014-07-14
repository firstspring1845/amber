package net.firsp.amber.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;

public class IconCache {

    static LruCache<String, Bitmap> memCache = new LruCache<String, Bitmap>(5242880); //5MB

    public static Bitmap getIcon(Context context, String sn, String url) {
        Bitmap b = memCache.get(url);
        if (b != null) {
            memCache.put(url, b);
            return b;
        }
        b = BitmapFactory.decodeFile(getFilePath(context, sn, url).getAbsolutePath());
        if (b != null) {
            //ImageViewが激最悪なのでここでリサイズ
            int imgSize = (int) (context.getResources().getDisplayMetrics().densityDpi / 160F * 48);
            b = Bitmap.createScaledBitmap(b, imgSize, imgSize, true);
            memCache.put(url, b);
        }
        return b;
    }

    public static void putIcon(Context context, String sn, String url, byte[] data) {
        //激リサイズ
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
        int imgSize = (int) (context.getResources().getDisplayMetrics().densityDpi / 160F * 48);
        b = Bitmap.createScaledBitmap(b, imgSize, imgSize, true);
        memCache.put(url, b);
        FileOutputStream fos = null;
        try {
            File cache = getFilePath(context, sn, url);
            cache.getParentFile().mkdirs();
            fos = new FileOutputStream(cache);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            try {
                fos.close();
            } catch (Exception e1) {
            }
        }
    }

    public static File getFilePath(Context context, String sn, String url) {
        File cache = new File(context.getCacheDir().getAbsolutePath(), "cache");
        File dir = new File(cache, sn);
        return new File(dir, getMD5String(url));
    }

    public static String getMD5String(String src) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(src.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : md5) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return ":(";
        }
    }
}
