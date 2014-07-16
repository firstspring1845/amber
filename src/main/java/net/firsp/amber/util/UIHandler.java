package net.firsp.amber.util;

import android.os.Handler;
import android.os.Looper;

public abstract class UIHandler extends Handler implements Runnable {

    public UIHandler(){
        super(Looper.getMainLooper());
        post(this);
    }

    @Override
    public abstract void run();

}
