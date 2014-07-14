package net.firsp.amber.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {

    public static void show(final Context context, final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void error(Context context) {
        show(context, "何かがおかしいです");
    }

}
