package net.firsp.amber.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CroutonUtil {

    public static final int INFO = 0;
    public static final int ALERT = 1;

    public static void showText(final Activity activity, final CharSequence charSequence, final int style) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Crouton.showText(activity, charSequence, getStyle(style));
                } catch (Exception e) {
                    DialogUtil.showException(activity, e);
                }
            }
        });
    }

    public static Style getStyle(final int type)
    {
        Configuration.Builder conf = new Configuration.Builder();
        conf.setDuration(1000);
        Style.Builder style = new Style.Builder();
        style.setConfiguration(conf.build());
        switch(type)
        {
            case INFO:
            {
                style.setBackgroundColorValue(Style.holoBlueLight);
                break;
            }
            case ALERT:
            {
                style.setBackgroundColorValue(Style.holoRedLight);
                break;
            }
        }
        return style.build();
    }

    public static void showText(Activity activity, CharSequence charSequence){
        showText(activity, charSequence, INFO);
    }

    public static void error(Activity activity){
        showText(activity, "何かがおかしいです:(", ALERT);
    }

}
