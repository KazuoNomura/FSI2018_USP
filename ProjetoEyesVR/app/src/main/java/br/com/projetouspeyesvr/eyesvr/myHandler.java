package br.com.projetouspeyesvr.eyesvr;

import android.graphics.Bitmap;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.logging.Handler;

public class myHandler extends android.os.Handler{
    private final WeakReference<MainActivity> mActivity;

    public myHandler(MainActivity activity) {
        mActivity = new WeakReference<MainActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        MainActivity activity = mActivity.get();
        if (activity != null) {
            try {
                activity.mLastFrame = (Bitmap) msg.obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    }
}
