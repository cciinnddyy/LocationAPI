package com.IFN702.gpslocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.os.AsyncTask;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.IFN702.gpslocation.mqttclient;

import java.util.ArrayList;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener
, OnConnectionFailedListener
{
    private static MainActivity context;
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;



    private TextView txtlocation;

    //General permission and supreme permission
    //Permission request and checking list

    private static final int ALL_PERMISSIONS_GRANT = 1111;
    private static final int PLAYSERVICE_PERMISSION = 3333;

    private ArrayList<String> permissionsTorequest;

    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();


    //update location settings
    private static final int INTERVEL = 5000, FASTEST_INTERVAL = 5000;

    public static MainActivity getContext(){

        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;


        txtlocation = findViewById(R.id.txtlocation);
        //Check for the permission despite the declaration in build.gradle already exist
        //Because they are the supreme permission

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsTorequest = permissionsTorequest(permissions);


        //build google api client
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(permissionsTorequest.size()>0){
                //The second parameter is to improve program readability
                requestPermissions(permissionsTorequest.toArray(new String[permissionsTorequest.size()]),ALL_PERMISSIONS_GRANT);
            }

        }


        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

    }

    private ArrayList<String> permissionsTorequest (ArrayList<String> wantedpermissions){

        ArrayList<String> results = new ArrayList<>();

        for(String permission : wantedpermissions){
            if(!hasPermission(permission)){

                results.add(permission);
            }
        }

        return results;

    }

    //Check if permission granted
    private boolean hasPermission(String permission){

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        else
            return true;
    }

    @Override
    protected void onStart(){
        super.onStart();

        if(googleApiClient!=null){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(checkPlayServices()){
            txtlocation.setText("Permission required");
        }

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAYSERVICE_PERMISSION);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
            // Permissions ok, we get last location
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (location != null) {
                txtlocation.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());

                //Mqtt client connection



            }

            startLocationUpdates();
        String latitude = String.valueOf(location.getLatitude());

        mclient.execute(latitude, String.valueOf(location.getLongitude()));
    }



        private void startLocationUpdates() {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(INTERVEL);
            locationRequest.setFastestInterval(FASTEST_INTERVAL);

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    &&  ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    mqttclient mclient = new mqttclient();

    public static String locations ;

    public String getLontitudeL(double lontitude, double lantitude){
        String location = String.valueOf(lontitude)+ "," + String.valueOf(lantitude);
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            txtlocation.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());

            locations = getLontitudeL(location.getLongitude(),location.getLatitude());

        }
    }








}
