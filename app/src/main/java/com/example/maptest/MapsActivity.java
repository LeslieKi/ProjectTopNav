package com.example.maptest;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;

    // Google Services client for APIs and other functions
    private GoogleApiClient mGoogleApiClient;

    // TAG variable for printing info to the log
    public static final String TAG = MapsActivity.class.getSimpleName();

    // app-defined constant for checking permission cases
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Constant static member to define request code to be sent to Google Play Services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private long UPDATE_INTERVAL = 10 * 1000; // 10 seconds, in milliseconds
    private long FASTEST_INTERVAL = 2000; // 1 second, in milliseconds

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        // Initializes the LocationRequest variable
        // Set priority to High Accuracy to request as accurate a location as possible
        // This takes more power and time, but is essential for a navigation app
        // Obviously use low time intervals since we're creating a navigation app
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
    }//end onCreate

    /**
     * Now that the client is built, we connect it
     * Use onResume() instead of onStart() because the activity may be paused
     * At any time, for example when a call or text message comes in
     * This allows the activity to be resumed at any time
     * onResume() is called right after onCreate()
     */
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }// end onResume

    /**
     * Whenever adding code in onResume(), we add corresponding code for onPause
     * Disconnect from location services when activity is paused
     * Verify the client is connected before disconnecting
     */
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }// end onPause

    /**
     * Once client is connected to location services, onConnect() is called
     * Obtain last location and log it
     */
    public void onConnected(Bundle bundle) {
        // TAG to check onConnected is being accessed
        Log.i(TAG, "Location services connected.");

        //Check for permission for location data
        //If permission is not granted, request it
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

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
    }// end onConnected

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }// end onConnectionSuspended

    public void onConnectionFailed(ConnectionResult connectionResult) {
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
     * This method is called any time a new location is detected by Google Play Services
     */
    public void onLocationChanged(Location location) {
        // Print passed location to the log
        Log.d(TAG, location.toString());

        // Use double for storing coordinates as this is what LatLng objects use
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        // Create new LatLng object from recent location
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        // Create a new market and display it on the map
        // MarkerOptions defines the options for the new marker
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(20));
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
        //Check for permission for location data
        //If permission is not granted, request it

        mMap = googleMap;
        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(20.0f);

        //Test JSON for styling the map
        /*MapStyleOptions style = new MapStyleOptions("[" +
                " {" +
                " \"featureType\":\"poi.school\"," +
                " \"elementType\": \"geometry\"," +
                " \"stylers\":[" +
                "    {" +
                "    \"color\": \"#ff3c3c\"" +
                "    }," +
                "    {" +
                "     \"saturation\": \"100\"" +
                "    }," +
                "    {" +
                "        \"lightness\": \"45\"" +
                "     }" +
                "    ]" +
                " }" +
                "]");*/

        // How the blue myLocation dot is enabled
        //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    return;
        // }
        //mMap.setMyLocationEnabled(true);

        LatLng WKU = new LatLng(36.985111, -86.455669);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(WKU));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

    }// end onMapReady
} // end class MapsActivity
