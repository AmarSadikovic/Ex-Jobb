package se.mah.af6260.exjobb;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private boolean startDialog = false;
    private boolean walkStart = false;
    private Stopwatch myStopwatch;
    private LatLng lastPos;
    private float distanceInMeters = 0;
    private boolean isWalkFinished = false;
    private File gpxfile;
    private ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int status = this.getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, this.getPackageName());
        if (status == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        while (status == PackageManager.PERMISSION_DENIED) {
            if (this.getPackageManager().checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, this.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                status = PackageManager.PERMISSION_GRANTED;
            }
        }
        actionBar = getSupportActionBar();
        actionBar.setTitle("Rör dig till blå markerat område");
        myStopwatch = new Stopwatch();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        currentGeofence = 0;

        mConnection = new MyServiceConnection(this);
        Intent soundIntent = new Intent(this, SoundPlayer.class);
        bindService(soundIntent, mConnection, Context.BIND_AUTO_CREATE);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                    Location locationl = locationResult.getLastLocation();
                    LatLng myPos = new LatLng(locationl.getLatitude(), locationl.getLongitude());

                    if(walkStart) {
                        //Textdokument location
                        //logToTextdoc(myPos);

                        //Check if walk is at end
                        if (!mService.isPlaying() && isWalkFinished) {
                            walkStart = false;
                            currentGeofence = 0;
                            for (int i = 1; i < geofences.size(); i++) {
                                geofences.get(i).setFillColor(0x3FFF0000);
                                geofences.get(i).setStrokeColor(0x4F9F0000);
                            }
                            geofences.get(currentGeofence).setFillColor(0x3F0000FF);
                            geofences.get(currentGeofence).setStrokeColor(0x4F00009F);

                            //ALERT FÖR AVSLUTAD VANDRING
                            TextView tv = new TextView(mainActivity);
                            tv.setMovementMethod(LinkMovementMethod.getInstance());
                            double ms = (distanceInMeters/myStopwatch.getSeconds());
                            tv.setText(Html.fromHtml("<H2>Medelhastighet(NOTERA): </H2><b> <H3>" + String.format("%.2f", ms) + " m/s</H3></b><br>" +
                                    "<H2>Besök länk för att besvara enkät: <a href='https://docs.google.com/forms/d/16AMvDPzxSQlolzcf8UdvNi6B2DOcJIR1A6YK9rRMrVg/prefill'>Click Here</a></H2>"));
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Vandring klar!");
                            alertDialog.setView(tv);
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        } else if(walkStart){

                            if (!(PolyUtil.containsLocation(myPos, geofences.get(currentGeofence - 1).getPoints(), false)) && !(PolyUtil.containsLocation(myPos, geofences.get(currentGeofence).getPoints(), false))) {
                                if (!mService.isPause()) {
                                    mService.pauseSound();
                                    myStopwatch.pauseTimer();
                                    actionBar.setTitle("Pausad Geofence #" + currentGeofence);
                                    geofences.get(currentGeofence - 1).setFillColor(0x3FFFFF00);
                                    geofences.get(currentGeofence - 1).setStrokeColor(0x4F9F9F00);
                                }
                                //Re-entered geofence when paused
                            } else if (mService.isPause()) {
                                mService.resume();
                                myStopwatch.resumeTimer();
                                actionBar.setTitle("I Geofence område " + currentGeofence + 1);
                                geofences.get(currentGeofence - 1).setFillColor(0x3F00FF00);
                                geofences.get(currentGeofence - 1).setStrokeColor(0x4F009F00);
                            }
                            //Enter new geofence
                            if (!isWalkFinished && PolyUtil.containsLocation(myPos, geofences.get(currentGeofence).getPoints(), false) && !mService.isPause() && !mService.isPlaying()) {
                                geofences.get(currentGeofence).setFillColor(0x3F00FF00);
                                geofences.get(currentGeofence).setStrokeColor(0x4F009F00);
                                actionBar.setTitle("I Geofence område " + (currentGeofence + 1));
                                mService.playSound(soundTracks.get(currentGeofence));
                                if (geofences.size() - 1 > currentGeofence) {
                                    currentGeofence++;
                                    geofences.get(currentGeofence).setFillColor(0x3F0000FF);
                                    geofences.get(currentGeofence).setStrokeColor(0x4F00009F);
                                } else {
                                    isWalkFinished = true;
                                }
                            }
                            if(!mService.isPause()){
                                float[] results = new float[1];
                                Location.distanceBetween(lastPos.latitude, lastPos.longitude, myPos.latitude, myPos.longitude, results);
                                distanceInMeters += results[0];
                                lastPos = myPos;
                            }
                        }
                    } else if(!startDialog) {
                        if (PolyUtil.containsLocation(myPos, geofences.get(currentGeofence).getPoints(), false)) {
                            lastPos = myPos;
                            TextView tv = new TextView(mainActivity);
                            tv.setMovementMethod(LinkMovementMethod.getInstance());
                            tv.setText(Html.fromHtml("Inside geofence # 1, Lite information om vandringen <br>" +
                            "<h2><font color='blue'>Blåa</font> Geofence områden:</h2> Är det nästa geofence du ska röra dig mot <br>" +
                            "<h2><font color='red'>Röda</font> Geofence områden:</h2> Är geofences som inte är aktiva ännu men som kommer användas i vandringen senare. <br>" +
                            "<h2><font color='#F9F9F00'>Gula</font> Geofence områden:</h2> Betyder att vandringen är pausad och du har rört dig utanför aktivt geofence område, gå tillbaka till gul markerat område för att återupta vandringen. <br> " +
                                    "<h2><font color='green'>Gröna</font> Geofence områden:</h2> Betyder att du har spelat eller spelar upp ljud för området. <br>" +
                            "<h2>För att starta vandringen klicka OK! Lycka till!</h2>"));
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("TestWalk");
                            alertDialog.setView(tv);
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            geofences.get(currentGeofence).setFillColor(0x3F00FF00);
                                            geofences.get(currentGeofence).setStrokeColor(0x4F009F00);
                                            actionBar.setTitle("I Geofence område " + (currentGeofence + 1));
                                            mService.playSound(soundTracks.get(currentGeofence));
                                            if (geofences.size() - 1 > currentGeofence) {
                                                currentGeofence++;
                                                geofences.get(currentGeofence).setFillColor(0x3F0000FF);
                                                geofences.get(currentGeofence).setStrokeColor(0x4F00009F);
                                            }
                                            walkStart = true;
                                            isWalkFinished = false;
                                            myStopwatch.startTimer();
                                            //logToTextdoc();
                                            startDialog = false;
                                        }
                                    });
                            alertDialog.show();
                            startDialog = true;
                        }
                    }
            };
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geofences = new ArrayList<Polygon>();

        soundTracks = new ArrayList<Integer>();

        soundTracks.add(R.raw.ljud1);
        soundTracks.add(R.raw.ljud2);
        soundTracks.add(R.raw.ljud3);
        soundTracks.add(R.raw.ljud4);
        soundTracks.add(R.raw.ljud5);
        soundTracks.add(R.raw.ljud6);
        soundTracks.add(R.raw.ljud7);
        soundTracks.add(R.raw.ljud8);
        soundTracks.add(R.raw.ljud9);
        soundTracks.add(R.raw.ljud10);
        soundTracks.add(R.raw.ljud11);
        soundTracks.add(R.raw.ljud12);

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
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
                            mMap.animateCamera(cameraUpdate);
                        }
                    }
                });
//        addTestPositions();
    }

    public void logToTextdoc(){
        try {
            Date currentTime = Calendar.getInstance().getTime();
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            gpxfile = new File(root, "Locations " + currentTime.toString() + ".txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append("Latitude    Longitude \n");
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void logToTextdoc(LatLng pos){
        try {
            FileWriter writer =  new FileWriter(gpxfile, true);
            writer.append(pos.latitude + " "  + pos.longitude + "\n");
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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

        //First geofence blue
        Polygon polygonstart = mMap.addPolygon(new PolygonOptions()
                .add(latLngs2.get(1-1),  latLngs2.get(1), latLngs1.get(1), latLngs1.get(1-1))
                .strokeColor(0x3F0000FF)
                .fillColor(0x4F00009F));
        geofences.add(polygonstart);
        //Rest red
        for(int i = 2; i < latLngs1.size(); i++){
            Polygon polygon = mMap.addPolygon(new PolygonOptions()
                    .add(latLngs2.get(i-1),  latLngs2.get(i), latLngs1.get(i), latLngs1.get(i-1))
                    .strokeColor(0x4F9F0000)
                    .fillColor(0x3FFF0000));
            geofences.add(polygon);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null );
    }

    public void addTestPositions(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("location_test_1.txt")));
            String line;
            reader.readLine();
            int counter = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println("new marker");
                String[] RowData = line.split(" ");
                LatLng latLng = new LatLng(Double.valueOf(RowData[0]), Double.valueOf(RowData[1]));
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .title("Marker " + counter));
                counter++;
                reader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
