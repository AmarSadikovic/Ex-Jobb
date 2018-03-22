package se.mah.af6260.exjobb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private List<Polygon> geofences;
    private List<Integer> soundTracks;
    private int currentGeofence;
    private MainActivity mainActivity = this;

    private MyServiceConnection mConnection;
    public boolean mBound;
    public SoundPlayer mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        currentGeofence = 0;

        mConnection = new MyServiceConnection(this);
        Intent soundIntent = new Intent(this, SoundPlayer.class);
        bindService(soundIntent, mConnection, Context.BIND_AUTO_CREATE);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Location locationl = locationResult.getLastLocation();
                    LatLng myPos = new LatLng(locationl.getLatitude(), locationl.getLongitude());

                    if(PolyUtil.containsLocation(myPos, geofences.get(currentGeofence).getPoints(), false) && !mService.isPlaying()){
                        geofences.get(currentGeofence).setFillColor(0x3F00FF00);
                        geofences.get(currentGeofence).setStrokeColor(0x4F009F00);
                        Toast.makeText(mainActivity, "Inside polygon " + currentGeofence, Toast.LENGTH_LONG).show();
                        mService.playSound(soundTracks.get(currentGeofence));
                        if(geofences.size()-1 > currentGeofence) {
                            currentGeofence++;
                            geofences.get(currentGeofence).setFillColor(0x3F0000FF);
                            geofences.get(currentGeofence).setStrokeColor(0x4F00009F);
                        } else {
                            currentGeofence = 0;
                            for(int i = 0 ; i< geofences.size(); i++){
                                geofences.get(i).setFillColor(0x3FFF0000);
                                geofences.get(i).setStrokeColor(0x4F9F0000);
                            }
                        }
                    } else {
                        geofences.get(currentGeofence).setFillColor(0x3F0000FF);
                        geofences.get(currentGeofence).setStrokeColor(0x4F00009F);
                        if(mService.isPlaying()){
                            if(!(PolyUtil.containsLocation(myPos, geofences.get(currentGeofence-1).getPoints(), false))
                            && !(PolyUtil.containsLocation(myPos, geofences.get(currentGeofence).getPoints(), false))){
                                mService.pauseSound();
                                geofences.get(currentGeofence-1).setFillColor(0x3FFFFF00);
                                geofences.get(currentGeofence-1).setStrokeColor(0x4F9F9F00);
                            } else if(mService.isPause()) {
                                mService.resume();
                                geofences.get(currentGeofence-1).setFillColor(0x3F00FF00);
                                geofences.get(currentGeofence-1).setStrokeColor(0x4F009F00);
                            }
                        }
                    }

                    System.out.println("new location " + " LAT "  + myPos.latitude + " Long " + myPos.longitude);
                }
            };
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geofences = new ArrayList<Polygon>();

        soundTracks = new ArrayList<Integer>();

        soundTracks.add(R.raw.airplane);
        soundTracks.add(R.raw.seagull);
        soundTracks.add(R.raw.slime);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        startLocationUpdates();
        addGeofences();
    }


    private void addGeofences(){

        ArrayList<LatLng> latLngs1 = new ArrayList<LatLng>();
        ArrayList<LatLng> latLngs2 = new ArrayList<LatLng>();
        try {
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(getAssets().open("vandring_latlng_p1.txt")));
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(getAssets().open("vandring_latlng_p2.txt")));
            String line;
            reader1.readLine();
            reader2.readLine();

            while ((line = reader1.readLine()) != null) {
                String[] RowData = line.split(",");
                LatLng latLng = new LatLng(Double.valueOf(RowData[0]), Double.valueOf(RowData[1]));
                latLngs1.add(latLng);
            }
            while ((line = reader2.readLine()) != null) {
                String[] RowData = line.split(",");
                LatLng latLng = new LatLng(Double.valueOf(RowData[0]), Double.valueOf(RowData[1]));
                latLngs2.add(latLng);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for(int i = 1; i < latLngs1.size(); i++){
            System.out.println(i + "      " + latLngs1.size() + "         " + latLngs2.size());
            Polygon polygon = mMap.addPolygon(new PolygonOptions()
                    .add(latLngs2.get(i-1),  latLngs2.get(i), latLngs1.get(i), latLngs1.get(i-1))
                    .strokeColor(0x4F9F0000)
                    .fillColor(0x3FFF0000));
            geofences.add(polygon);
        }




//        Polygon polygon2 = mMap.addPolygon(new PolygonOptions()
//                .add(new LatLng(55.610745, 12.994912), new LatLng(55.610726, 12.994483), new LatLng(55.610094, 12.994680), new LatLng(55.610103, 12.995077) )
//                .strokeColor(0x4F9F0000)
//                .fillColor(0x3FFF0000));
//        geofences.add(polygon2);
//        Polygon polygon3 = mMap.addPolygon(new PolygonOptions()
//                .add(new LatLng(55.610103, 12.995077), new LatLng(55.610094, 12.994680), new LatLng(55.609048, 12.994943), new LatLng(55.609090 , 12.995313))
//                .strokeColor(0x4F9F0000)
//                .fillColor(0x3FFF0000));
//        geofences.add(polygon3);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null );
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
