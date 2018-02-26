package se.mah.exjobb.exjobb;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.LocationSource;

import java.util.Map;

/**
 * Created by oskar on 2018-02-26.
 */

public class MyLocation extends Service implements LocationSource, LocationListener {
    private LocationManager locationManager;
    private MapsActivity mapsActivity;
    private LocalBinder mBinder;
    private OnLocationChangedListener listener;


    public void onCreate() {
        super.onCreate();
        mBinder = new LocalBinder();

    }

    public void setListenerActivity(MapsActivity activity) {
        this.mapsActivity = activity;

        locationManager = (LocationManager) mapsActivity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mapsActivity.onLocationChanged(location);
        if(listener != null)
        {
            listener.onLocationChanged(location);
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.listener = listener;
        System.out.println("ACTIVATEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEe ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    public void deactivate() {
        locationManager.removeUpdates(this);
    }


    public class LocalBinder extends Binder {
         MyLocation getService() {
            return MyLocation.this;
        }
    }
}
