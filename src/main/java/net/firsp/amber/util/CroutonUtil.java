package net.firsp.amber.util;

import android.app.Activity;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CroutonUtil {

    public static final int INFO = 0;
    public static final int ALERT = 1;

    public static void showText(Activity activity, CharSequence charSequence, int style) {
        new UIHandler().post(() -> {
            Crouton.showText(activity, charSequence, getStyle(style));
        });
    }

    public static Style getStyle(int type) {
        Configuration.Builder conf = new Configuration.Builder();
        conf.setDuration(1000);
        Style.Builder style = new Style.Builder();
        style.setConfiguration(conf.build());
        switch (type) {
            case INFO: {
                style.setBackgroundColorValue(Style.holoBlueLight);
                break;
            }
            case ALERT: {
                style.setBackgroundColorValue(Style.holoRedLight);
                break;
            }
        }
        return style.build();
    }

    public static void showText(Activity activity, CharSequence charSequence) {
        showText(activity, charSequence, INFO);
    }

    public static void error(Activity activity) {
        showText(activity, "何かがおかしいです:(", ALERT);
    }

}
