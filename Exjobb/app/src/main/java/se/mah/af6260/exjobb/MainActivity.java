package se.mah.af6260.exjobb;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private List<Polygon> polygons;
    private int polygonCounter = 0;
    private MainActivity mainActivity = this;
//    private GeofencingClient mGeofencingClient;
//    private PendingIntent mGeofencePendingIntent;
//    private GeofencingRequest mGeofenceRequest;
//    private List<Geofence> mGeofenceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
//                    mMap.addMarker(new MarkerOptions()
//                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
//                            .title("Hello world"));
                    Location locationl = locationResult.getLastLocation();
                    LatLng myPos = new LatLng(locationl.getLatitude(), locationl.getLongitude());
                    if(PolyUtil.containsLocation(myPos, polygons.get(0).getPoints(), false)){
                        polygons.get(0).setFillColor(0x3F00FF00);
                        polygons.get(0).setStrokeColor(0x4F009F00);
                        Toast.makeText(mainActivity, "Inside polygon " + 0, Toast.LENGTH_LONG).show();
                    } else {
                        polygons.get(0).setFillColor(0x3FFF0000);
                        polygons.get(0).setStrokeColor(0x4F9F0000);
                    }

                    if(PolyUtil.containsLocation(myPos, polygons.get(1).getPoints(), false)){
                        polygons.get(1).setFillColor(0x3F00FF00);
                        polygons.get(1).setStrokeColor(0x4F009F00);
                        Toast.makeText(mainActivity, "Inside polygon " + 1, Toast.LENGTH_LONG).show();
                    } else {
                        polygons.get(1).setFillColor(0x3FFF0000);
                        polygons.get(1).setStrokeColor(0x4F9F0000);
                    }

                    if(PolyUtil.containsLocation(myPos, polygons.get(2).getPoints(), false)){
                        polygons.get(2).setFillColor(0x3F00FF00);
                        polygons.get(2).setStrokeColor(0x4F009F00);
                        Toast.makeText(mainActivity, "Inside polygon " + 2, Toast.LENGTH_LONG).show();
                    } else {
                        polygons.get(2).setFillColor(0x3FFF0000);
                        polygons.get(2).setStrokeColor(0x4F9F0000);
                    }

                    System.out.println("new location " + " LAT "  + myPos.latitude + " Long " + myPos.longitude);
                }
            };
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        polygons = new ArrayList<Polygon>();
//        mGeofenceList = new ArrayList<Geofence>();
//        mGeofencePendingIntent = null;
//        mGeofencingClient = LocationServices.getGeofencingClient(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        startLocationUpdates();
        Polygon polygon = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(55.611205, 12.994459), new LatLng(55.611213, 12.994691), new LatLng(55.610791, 12.994818), new LatLng(55.610777, 12.994524))
                .strokeColor(0x4F00009F)
                .fillColor(0x3F0000FF));
        polygons.add(polygon);
        Polygon polygon2 = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(55.610791, 12.994818), new LatLng(55.610777, 12.994524), new LatLng(55.610071, 12.994729), new LatLng(55.610102, 12.995018))
                .strokeColor(0x4F00009F)
                .fillColor(0x3F0000FF));
        polygons.add(polygon2);
        Polygon polygon3 = mMap.addPolygon(new PolygonOptions()
                .add(new LatLng(55.610071, 12.994729), new LatLng(55.610102, 12.995018), new LatLng(55.609044, 12.995282), new LatLng(55.609016 , 12.994995))
                .strokeColor(0x4F00009F)
                .fillColor(0x3F0000FF));
        polygons.add(polygon3);
//        createGeofence(new LatLng(55.688984, 13.174774));
//        addGeofences();

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


    //GEOFENCE

//    public void createRectGeofence(LatLng latLng) {
//        mGeofenceList.add(new Geofence.Builder()
//                // Set the request ID of the geofence. This is a string to identify this
//                // geofence.
//                .setRequestId("test")
//                .setCircularRegion(
//                        latLng.latitude,
//                        latLng.longitude,
//                        50
//                )
//
//                .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build());
//
//        CircleOptions circleOptions = new CircleOptions()
//                .center( new LatLng(latLng.latitude, latLng.longitude) )
//                .radius( 50 )
//                .fillColor(0x40ff0000)
//                .strokeColor(Color.TRANSPARENT)
//                .strokeWidth(2);
//        mMap.addCircle(circleOptions);
//    }
//
//    public void createGeofence(LatLng latLng) {
//        mGeofenceList.add(new Geofence.Builder()
//                // Set the request ID of the geofence. This is a string to identify this
//                // geofence.
//                .setRequestId("test")
//                .setCircularRegion(
//                        latLng.latitude,
//                        latLng.longitude,
//                        50
//                )
//
//                .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build());
//
//        CircleOptions circleOptions = new CircleOptions()
//                .center( new LatLng(latLng.latitude, latLng.longitude) )
//                .radius( 50 )
//                .fillColor(0x40ff0000)
//                .strokeColor(Color.TRANSPARENT)
//                .strokeWidth(2);
//        mMap.addCircle(circleOptions);
//    }
//
//    public void addGeofences() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        System.out.println("Geofences added");
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        System.out.println("Geofences FAILED to add");
//                    }
//                });
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        // Reuse the PendingIntent if we already have it.
//        if (mGeofencePendingIntent != null) {
//            return mGeofencePendingIntent;
//        }
//        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
//        // calling addGeofences() and removeGeofences().
//        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return mGeofencePendingIntent;
//    }
//
//    private GeofencingRequest getGeofencingRequest() {
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
//        builder.addGeofences(mGeofenceList);
//        return builder.build();
//    }
}
