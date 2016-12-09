package com.example.maptest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainScreen extends AppCompatActivity {

    //GG People!
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    public static final String TAG = MainScreen.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In: MainScreen | Method: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        Log.i(TAG, "In: MainScreen | Method: checkLocationPermissions()");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            // Request it
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "In: MainScreen | Method: onRequestPermissionsResult()");
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission was granted
            Toast.makeText(this, "Access Location permission granted", Toast.LENGTH_SHORT).show();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    protected void onResumeFragments() {
        Log.i(TAG, "In: MainScreen | Method: onResumeFragments()");
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        Log.i(TAG, "In: MainScreen | Method: showMissingPermissionError()");
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void loadMap(View view) {
        Log.i(TAG, "In: MainScreen | Method: loadMap()");
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    }
