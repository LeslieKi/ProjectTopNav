package com.example.maptest;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.example.maptest.R.id.map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {

    //timers
    long start;
    long start1;
    long start2;
    long start3;
    long end3;
    long end2;
    long end1;
    long end;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;

    protected ArrayList<Geofence> mGeofenceList;

    private PendingIntent mGeofencePendingIntent;

    // used to build location request client
    private LocationRequest mLocationRequest;

    // Google Services client for APIs and other functions
    private GoogleApiClient mGoogleApiClient;

    // TAG variable for printing info to the log
    public static final String TAG = MapsActivity.class.getSimpleName();

    // Constant static member to define request code to be sent to Google Play Services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // variables to set location update interval
    private long UPDATE_INTERVAL = 30 * 1000; // 30 seconds, in milliseconds
    private long FASTEST_INTERVAL = 10 * 1000; // 10 second, in milliseconds

    //LatLng object for cherryHall, used for geofence and marker
    private LatLng cherryHall = new LatLng(36.987336, -86.451221);

    //sets how long the geofence will exist
    private static final long GEO_DURATION = 100 * 1000;

    //string that provides the ID for the geofence
    private static final String GEOFENCE_REQ_ID = "Cherry Hall";

    //the active area for the geofence
    private static final float GEOFENCE_RADIUS = 50.0f; // in meters

    //circle object that provides a visual reference for the geofence
    private Circle geoFenceLimits;

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In: MapsActivity | Method: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        start1 = System.nanoTime();
        // Create the API client to start receiving Google Services
        // Multiple APIs can be passed in here
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // First two lines signify this class is handling connections
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                // APIs to utilize are added here
                .addApi(LocationServices.API)
                // Builds the client
                .build();
        end1 = System.nanoTime();
        Log.i(TAG, "Time to build GoogleApiClient = "+(end1-start1)/1000000+ "ms");

        start2 = System.nanoTime();
        // Initializes the LocationRequest variable
        // Set priority to High Accuracy to request as accurate a location as possible
        // This takes more power and time, but is essential for a navigation app
        // Obviously use low time intervals since we're creating a navigation app
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        end2 = System.nanoTime();
        Log.i(TAG, "Time to start LocationServices = "+(end2-start2)/1000000+ "ms");
    }//end onCreate

    /**
     * Now that the client is built, we connect it
     * Use onResume() instead of onStart() because the activity may be paused
     * At any time, for example when a call or text message comes in
     * This allows the activity to be resumed at any time
     * onResume() is called right after onCreate()
     */
    protected void onResume() {
        Log.i(TAG, "In: MapsActivity | Method: onResume().");
        super.onResume();
        mGoogleApiClient.connect();
    }// end onResume

    /**
     * Whenever adding code in onResume(), we add corresponding code for onPause
     * Disconnect from location services when activity is paused
     * Verify the client is connected before disconnecting
     */
    protected void onPause() {
        Log.i(TAG, "In: MapsActivity | Method: onPause().");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }// end onPause

    /**
     * Check for permission to access location
     */
    private boolean checkPermission() {
        Log.d(TAG, "In: MapsActivity | Method: checkPermission()");
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Once client is connected to location services, onConnect() is called
     * Obtain last location and log it
     */
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "In: MapsActivity | Method: onConnected()");
        // TAG to check onConnected is being accessed
        Log.i(TAG, "Location services connected.");

        //Check for permission for location data
        //If permission is not granted, request it
        checkPermission();

        start3 = System.nanoTime();
        // Location variable for storing last location
        // Note: this may be null if the last location is not already known
        // For example, the first time Google Play services checks for location
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            Log.d(TAG, "Location null");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            onLocationChanged(location);
        }

        startGeofence();
    }// end onConnected

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "In: MapsActivity | Method: onConnectionSuspended()");
        Log.i(TAG, "Location services suspended. Please reconnect.");
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }// end onConnectionSuspended

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "In: MapsActivity | Method: onConnectionFailed()");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }// end onConnectionFailed

    /**
     * All of the following code is for gathering the user's location
     */

    //This method is called any time Google Play Service's detects a change in location
    public void onLocationChanged(Location location) {
        Log.i(TAG, "In: MapsActivity | Method: onLocationChanged()");
        // Print passed location to the log
        Log.d(TAG, location.toString());

        // Use double for storing coordinates as this is what LatLng objects use
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        // Create new LatLng object from recent location
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        end3 = System.nanoTime();
        Log.i(TAG, "Time to get first location update = "+(end3-start3)/1000000+ "ms");

        //move the camera to the user's position
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }// end onLocationChanged

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "In: MapsActivity | Method: onMapReady()");
        //Check for permission for location data
        //If permission is not granted, request it
        checkPermission();

        mMap = googleMap;

        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(20.0f);

        // How the blue myLocation dot is enabled
        mMap.setMyLocationEnabled(true);

        //startGeofence();
    }// end onMapReady

    /**
     * All of the following code is for building and handling Geofences
     */
    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "In: MapsActivity | Method: startGeofence()");
        start = System.nanoTime();
        Geofence geofence = createGeofence(cherryHall, GEOFENCE_RADIUS);
        GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
        addGeofence(geofenceRequest);
    }

    // Create a Geofence with the builder
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "In: MapsActivity | Method: createGeofence()");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(2000)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "In: MapsActivity | Method: createGeofenceRequest()");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofence(geofence)
                .build();
    }

    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "In: MapsActivity | Method: createGeofencePendingIntent()");
        if (mGeofencePendingIntent != null) {
            Log.d(TAG, "Using existing geofence intent");
            return mGeofencePendingIntent;
        } else { Log.d(TAG, "geofence intent is null, creating new intent");
            Intent intent = new Intent(this, GeofenceTransitionService.class);
            return PendingIntent.getService(
                    this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "In: MapsActivity | Method: addGeofence()");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    //this method is called after the geofence request
    //determines if the build was successful or not
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "In: MapsActivity | Method: onResult: " + status);
        if (status.isSuccess()) {
            Log.d(TAG, "Geofence was created");
            end = System.nanoTime();
            Log.i(TAG, "Time to create geofence = "+(end-start)/1000000+ "ms");
            drawGeofence();
        } else {
            Log.d(TAG, "Geofence failed to create");
        }
    }

    private void drawGeofence() {
        Log.d(TAG, "In: MapsActivity | Method: drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(cherryHall)
                .strokeColor(Color.argb(30, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = mMap.addCircle(circleOptions);

        mMap.addMarker(new MarkerOptions()
                .position(cherryHall)
                .title("Cherry Hall"));
    }
} // end class MapsActivity
