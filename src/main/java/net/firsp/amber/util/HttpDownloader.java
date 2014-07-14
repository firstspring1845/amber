package net.firsp.amber.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloader {

    public static byte[] download(String url) {
        try {
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
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
