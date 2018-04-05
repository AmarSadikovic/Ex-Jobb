package se.mah.af6260.exjobb;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by oskar on 2018-03-14.
 */

public class MyServiceConnection implements ServiceConnection {
    private final MainActivity mActivity;

    public MyServiceConnection(MainActivity a){
        mActivity = a;
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        SoundPlayer.LocalBinder binder = (SoundPlayer.LocalBinder) service;
        mActivity.mService = binder.getService();
        mActivity.mBound = true;
        mActivity.mService.setListenerActivity(mActivity);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mActivity.mBound = false;
    }
}
