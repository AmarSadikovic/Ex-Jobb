package se.mah.exjobb.exjobb;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by oskar on 2018-02-26.
 */

public class MyServiceConnection implements ServiceConnection {
    private final MapsActivity mActivity;

    public MyServiceConnection(MapsActivity a){

        mActivity = a;
    }
    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        MyLocation.LocalBinder binder = (MyLocation.LocalBinder) service;
//        mActivity.mService = binder.getService();
//        mActivity.mBound = true;
//        mActivity.mService.setListenerActivity(mActivity);
        Toast.makeText(mActivity, "Service bound", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onServiceDisconnected(ComponentName arg0) {

//        mActivity.mBound = false;
        Toast.makeText(mActivity, "Service unbound", Toast.LENGTH_SHORT).show();
    }
}
